package fr.proline.mzscope.mzml;

import fr.proline.mzscope.mzml.Scan;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.proline.mzscope.utils.Base64;

public class ScanFactory {

	private static Logger logger = LoggerFactory.getLogger(ScanFactory.class);
	
	public enum Precision {
		FLOAT64("64-bit float", 8), FLOAT32("32-bit float", 4);

		String encoding;
		int sizeof;

		Precision(String encoding, int sizeof) {
			this.encoding = encoding;
			this.sizeof = sizeof;
		}

	}

	public static Scan readScan(PreScan preScan) {
		Scan scan = null;
		try {
		Precision p = Precision.FLOAT32.encoding.equals(preScan.masses_encoding) ? Precision.FLOAT32 : Precision.FLOAT64;
		float[] masses = decode(preScan.encodedMasses, p);
		p = Precision.FLOAT32.encoding.equals(preScan.intensities_encoding) ? Precision.FLOAT32 : Precision.FLOAT64;
		float[] intensities = decode(preScan.encodedIntensities, p);
		float rt  = preScan.rt != null ? Float.parseFloat(preScan.rt) : -1.0f;
		scan = new Scan(preScan.index, rt, masses, intensities);
		} catch (Exception e) {
			logger.error("error while reading scan "+preScan.id, e);
		}
		return scan;
	}

	public static float[] decode(String s, Precision precision) {
		byte[] data = Base64.decode(s);
		float[] resultArray = new float[data.length / precision.sizeof];
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		for (int indexOut = 0; indexOut < data.length; indexOut += precision.sizeof) {
			switch (precision) {
			case FLOAT64:
				resultArray[indexOut / precision.sizeof] = (float) bb.getDouble(indexOut);
				break;
			case FLOAT32:
				resultArray[indexOut / precision.sizeof] = bb.getFloat(indexOut);
				break;

			default:
				break;
			}
			
		}

		return resultArray;
	}

	public static List<Scan> read(String filepath) {

		
		final BlockingQueue<PreScan> queue = new LinkedBlockingQueue<PreScan>();
		final PreScan closingQueue = new PreScan();
		final List<Scan> result = new ArrayList<Scan>();
		
		final Runnable target = new Runnable() {

			public void run() {
				boolean goOn = true;

				while (goOn) {
					PreScan obj = null;

					try {
						obj = queue.take(); // Block
					} catch (InterruptedException intEx) {
						// Obj == null => Interrupted
					}

					if ((obj != null) && (obj != closingQueue)) {
						result.add(readScan(obj));

					} else {
						goOn = false; // Stop infinite loop
					}

				}

			}

		};

		final Thread thr = new Thread(target, "Thread-PreScan-handler");
		thr.setPriority(Thread.NORM_PRIORITY);
		thr.start();
		
		mzMLReader.readPreScan(filepath, queue);
		queue.add(closingQueue);

		try {
			thr.join();
		} catch (InterruptedException e) {
			logger.error("Thread interrupted", e);
		}
		
		return result;
	}
}
