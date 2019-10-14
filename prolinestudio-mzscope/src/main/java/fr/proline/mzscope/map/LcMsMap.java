/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.mzscope.map;

import java.io.File;
import java.io.FileNotFoundException;

import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.io.reader.cache.MzDbEntityCache;

/**
 * @author JeT
 *
 */
public class LcMsMap {
    private String filename = null;
    private File file = null;
    private MzDbEntityCache cache = new MzDbEntityCache();

    public LcMsMap(String filename) {
	super();
	this.filename = filename;
	this.file = new File(this.filename);
    }

    /**
     * @return the filename
     */
    public String getFilename() {
	return this.filename;
    }

    /**
     * @return the file
     */
    public File getFile() {
	return this.file;
    }

    /**
     * @return the cache
     */
    public MzDbEntityCache getCache() {
	return this.cache;
    }

    /**
     * Create a new reader for the given file. Stores and reuses entity cache
     *
     * @return
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLiteException
     */
    public MzDbReader createReader() throws ClassNotFoundException, FileNotFoundException, SQLiteException {
	return new MzDbReader(this.file, this.cache, false);
    }

    /**
     * get a a chunk of Lc MS map
     *
     * @param minMz
     * @param maxMz
     * @param minRt
     * @param maxRt
     */
    public LcMsMapChunk extractChunk(double minMz, double maxMz, double minRt, double maxRt) {
	return new LcMsMapChunk(this, new LcMsViewport(minMz, maxMz, minRt, maxRt));
    }

    /**
     * get a a chunk of Lc MS map
     *
     * @param viewport
     */
    public LcMsMapChunk extractChunk(final LcMsViewport viewport) {
	return new LcMsMapChunk(this, viewport);
    }
}
