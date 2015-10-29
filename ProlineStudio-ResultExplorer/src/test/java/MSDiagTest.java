
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import fr.proline.studio.rsmexplorer.gui.RsetMSDiagPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.MSDiagDialog;
	/**
	 * test the custom export dialog
	 *
	 * @author MB243701
	 */
	public class MSDiagTest extends JFrame {

	   /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	// private CustomExportDialog exportDialog;
		private static MSDiagDialog msdiagDialog;

	    public MSDiagTest() {
	        super("MSDiag test panel");
	        init();
	    }

	    private void init() {
	        JPanel panel = new JPanel();
	        panel.setLayout(new BorderLayout());
	        JButton exportButton = new JButton("Launch MSDiag");
	        msdiagDialog = MSDiagDialog.getDialog(this); //(this, false, true);
	       // final String defaultConfig = "{\"format\":\"xlsx\",\"decimal_separator\":\".\",\"date_format\":\"YYYY:MM:DD HH:mm:ss\",\"title_separator\":\"_\",\"data_export\":{\"all_protein_set\":true,\"best_profile\":true},\"sheets\":[{\"id\":\"information\",\"title\":\"search settings and infos\",\"presentation\":\"rows\",\"fields\":[{\"id\":\"information_project_name\",\"title\":\"project_name\",\"default_displayed\":true},{\"id\":\"information_result_set_name\",\"title\":\"result_set_name\",\"default_displayed\":true},{\"id\":\"information_search_title\",\"title\":\"search_title\",\"default_displayed\":true},{\"id\":\"information_search_date\",\"title\":\"search_date\",\"default_displayed\":true},{\"id\":\"information_raw_file_name\",\"title\":\"raw_file_name\",\"default_displayed\":true},{\"id\":\"information_peaklist_file_path\",\"title\":\"peaklist_file_path\",\"default_displayed\":true},{\"id\":\"information_result_file_name\",\"title\":\"result_file_name\",\"default_displayed\":true},{\"id\":\"information_result_file_directory\",\"title\":\"result_file_directory\",\"default_displayed\":true},{\"id\":\"information_job_number\",\"title\":\"job_number\",\"default_displayed\":true},{\"id\":\"information_user_name\",\"title\":\"user_name\",\"default_displayed\":true},{\"id\":\"information_user_email\",\"title\":\"user_email\",\"default_displayed\":true},{\"id\":\"information_queries_count\",\"title\":\"queries_count\",\"default_displayed\":true},{\"id\":\"information_submitted_queries_count\",\"title\":\"submitted_queries_count\",\"default_displayed\":true},{\"id\":\"information_searched_sequences_count\",\"title\":\"searched_sequences_count\",\"default_displayed\":true},{\"id\":\"information_software_name\",\"title\":\"software_name\",\"default_displayed\":true},{\"id\":\"information_software_version\",\"title\":\"software_version\",\"default_displayed\":true},{\"id\":\"information_instrument_config\",\"title\":\"instrument_config\",\"default_displayed\":true},{\"id\":\"information_database_names\",\"title\":\"database_names\",\"default_displayed\":true},{\"id\":\"information_database_releases\",\"title\":\"database_releases\",\"default_displayed\":true},{\"id\":\"information_taxonomy\",\"title\":\"taxonomy\",\"default_displayed\":true},{\"id\":\"information_enzymes\",\"title\":\"enzymes\",\"default_displayed\":true},{\"id\":\"information_max_missed_cleavages\",\"title\":\"max_missed_cleavages\",\"default_displayed\":true},{\"id\":\"information_fixed_ptms\",\"title\":\"fixed_ptms\",\"default_displayed\":true},{\"id\":\"information_variable_ptms\",\"title\":\"variable_ptms\",\"default_displayed\":true},{\"id\":\"information_peptide_charge_states\",\"title\":\"peptide_charge_states\",\"default_displayed\":true},{\"id\":\"information_peptide_mass_error_tolerance\",\"title\":\"peptide_mass_error_tolerance\",\"default_displayed\":true},{\"id\":\"information_fragment_mass_error_tolerance\",\"title\":\"fragment_mass_error_tolerance\",\"default_displayed\":true},{\"id\":\"information_is_decoy\",\"title\":\"is_decoy\",\"default_displayed\":true}],\"default_displayed\":true},{\"id\":\"import\",\"title\":\"import and filters\",\"presentation\":\"rows\",\"fields\":[{\"id\":\"information_result_file_name\",\"title\":\"result_file_name\",\"default_displayed\":true},{\"id\":\"import_params\",\"title\":\"import_params\",\"default_displayed\":true},{\"id\":\"import_psm_filter_expected_fdr\",\"title\":\"psm_filter_expected_fdr\",\"default_displayed\":true},{\"id\":\"import_psm_filter\",\"title\":\"psm_filter\",\"default_displayed\":true},{\"id\":\"import_prot_filter_expected_fdr\",\"title\":\"prot_filter_expected_fdr\",\"default_displayed\":true},{\"id\":\"import_prot_filter\",\"title\":\"import_prot_filter\",\"default_displayed\":true}],\"default_displayed\":true},{\"id\":\"protein_sets\",\"title\":\"protein sets\",\"presentation\":\"columns\",\"fields\":[{\"id\":\"protein_sets_id\",\"title\":\"protein_set_id\",\"default_displayed\":true},{\"id\":\"protein_sets_accession\",\"title\":\"accession\",\"default_displayed\":true},{\"id\":\"protein_sets_description\",\"title\":\"description\",\"default_displayed\":true},{\"id\":\"protein_sets_score\",\"title\":\"protein_set_score\",\"default_displayed\":true},{\"id\":\"protein_sets_is_validated\",\"title\":\"is_validated\",\"default_displayed\":true},{\"id\":\"protein_sets_selection_level\",\"title\":\"selection_level\",\"default_displayed\":false},{\"id\":\"protein_sets_nb_sameset_protein_matches\",\"title\":\"#sameset_protein_matches\",\"default_displayed\":true},{\"id\":\"protein_sets_nb_subset_protein_matches\",\"title\":\"#subset_protein_matches\",\"default_displayed\":true},{\"id\":\"protein_sets_coverage\",\"title\":\"coverage\",\"default_displayed\":true},{\"id\":\"protein_sets_mw\",\"title\":\"MW\",\"default_displayed\":true},{\"id\":\"protein_sets_nb_sequences\",\"title\":\"#sequences\",\"default_displayed\":true},{\"id\":\"protein_sets_nb_specific_sequences\",\"title\":\"#specific_sequences\",\"default_displayed\":true},{\"id\":\"protein_sets_nb_peptides\",\"title\":\"#peptides\",\"default_displayed\":true},{\"id\":\"protein_sets_nb_specific_peptides\",\"title\":\"#specific_peptides\",\"default_displayed\":true},{\"id\":\"protein_sets_nb_peptide_matches\",\"title\":\"#peptide_matches\",\"default_displayed\":true},{\"id\":\"protein_sets_nb_specific_peptide_matches\",\"title\":\"#specific_peptide_matches\",\"default_displayed\":true}],\"default_displayed\":true},{\"id\":\"best_psm\",\"title\":\"best PSM from protein sets\",\"presentation\":\"columns\",\"fields\":[{\"id\":\"psm_peptide_id\",\"title\":\"peptide_id\",\"default_displayed\":true},{\"id\":\"psm_sequence\",\"title\":\"sequence\",\"default_displayed\":true},{\"id\":\"psm_modifications\",\"title\":\"modifications\",\"default_displayed\":true},{\"id\":\"psm_score\",\"title\":\"psm_score\",\"default_displayed\":true},{\"id\":\"psm_calculated_mass\",\"title\":\"calculated_mass\",\"default_displayed\":true},{\"id\":\"psm_charge\",\"title\":\"charge\",\"default_displayed\":true},{\"id\":\"psm_experimental_moz\",\"title\":\"experimental_moz\",\"default_displayed\":true},{\"id\":\"psm_delta_moz\",\"title\":\"delta_moz\",\"default_displayed\":true},{\"id\":\"psm_rt\",\"title\":\"rt\",\"default_displayed\":true},{\"id\":\"psm_peptide_length\",\"title\":\"peptide_length\",\"default_displayed\":true},{\"id\":\"psm_initial_query_id\",\"title\":\"initial_query_id\",\"default_displayed\":true},{\"id\":\"psm_missed_cleavages\",\"title\":\"missed_cleavages\",\"default_displayed\":true},{\"id\":\"psm_rank\",\"title\":\"rank\",\"default_displayed\":true},{\"id\":\"psm_cd_pretty_rank\",\"title\":\"cd_pretty_rank\",\"default_displayed\":true},{\"id\":\"psm_fragment_matches_count\",\"title\":\"fragment_matches_count\",\"default_displayed\":true},{\"id\":\"psm_spectrum_title\",\"title\":\"spectrum_title\",\"default_displayed\":true},{\"id\":\"psm_nb_protein_sets\",\"title\":\"#protein_sets\",\"default_displayed\":true},{\"id\":\"psm_nb_protein_matches\",\"title\":\"#protein_matches\",\"default_displayed\":true},{\"id\":\"psm_nb_databank_protein_matches\",\"title\":\"#databank_protein_matches\",\"default_displayed\":true},{\"id\":\"psm_start\",\"title\":\"start\",\"default_displayed\":true},{\"id\":\"psm_end\",\"title\":\"end\",\"default_displayed\":true},{\"id\":\"psm_residue_before\",\"title\":\"residue_before\",\"default_displayed\":true},{\"id\":\"psm_residue_after\",\"title\":\"residue_after\",\"default_displayed\":true},{\"id\":\"protein_sets_id\",\"title\":\"protein_set_id\",\"default_displayed\":true},{\"id\":\"protein_sets_accession\",\"title\":\"accession\",\"default_displayed\":true},{\"id\":\"protein_sets_description\",\"title\":\"description\",\"default_displayed\":true},{\"id\":\"protein_sets_score\",\"title\":\"protein_set_score\",\"default_displayed\":true},{\"id\":\"protein_sets_is_validated\",\"title\":\"is_validated\",\"default_displayed\":true},{\"id\":\"protein_sets_selection_level\",\"title\":\"selection_level\",\"default_displayed\":false},{\"id\":\"protein_sets_nb_sameset_protein_matches\",\"title\":\"#sameset_protein_matches\",\"default_displayed\":false},{\"id\":\"protein_sets_nb_subset_protein_matches\",\"title\":\"#subset_protein_matches\",\"default_displayed\":false},{\"id\":\"protein_sets_coverage\",\"title\":\"coverage\",\"default_displayed\":false},{\"id\":\"protein_sets_mw\",\"title\":\"MW\",\"default_displayed\":false},{\"id\":\"protein_sets_nb_sequences\",\"title\":\"#sequences\",\"default_displayed\":false},{\"id\":\"protein_sets_nb_specific_sequences\",\"title\":\"#specific_sequences\",\"default_displayed\":false},{\"id\":\"protein_sets_nb_peptides\",\"title\":\"#peptides\",\"default_displayed\":false},{\"id\":\"protein_sets_nb_specific_peptides\",\"title\":\"#specific_peptides\",\"default_displayed\":false},{\"id\":\"protein_sets_nb_peptide_matches\",\"title\":\"#peptide_matches\",\"default_displayed\":false},{\"id\":\"protein_sets_nb_specific_peptide_matches\",\"title\":\"#specific_peptide_matches\",\"default_displayed\":false}],\"default_displayed\":true},{\"id\":\"protein_match\",\"title\":\"protein matches in protein set\",\"presentation\":\"columns\",\"fields\":[{\"id\":\"protein_sets_id\",\"title\":\"protein_set_id\",\"default_displayed\":true},{\"id\":\"protein_sets_accession\",\"title\":\"accession\",\"default_displayed\":true},{\"id\":\"protein_sets_description\",\"title\":\"description\",\"default_displayed\":true},{\"id\":\"protein_match_is_typical_protein\",\"title\":\"is_typical_protein\",\"default_displayed\":true},{\"id\":\"protein_match_is_sameset\",\"title\":\"is_sameset\",\"default_displayed\":true},{\"id\":\"protein_match_peptide_set_score\",\"title\":\"peptide_set_score\",\"default_displayed\":true},{\"id\":\"protein_sets_coverage\",\"title\":\"coverage\",\"default_displayed\":true},{\"id\":\"protein_sets_mw\",\"title\":\"MW\",\"default_displayed\":true},{\"id\":\"protein_sets_nb_sequences\",\"title\":\"#sequences\",\"default_displayed\":true},{\"id\":\"protein_sets_nb_specific_sequences\",\"title\":\"#specific_sequences\",\"default_displayed\":true},{\"id\":\"protein_sets_nb_peptides\",\"title\":\"#peptides\",\"default_displayed\":true},{\"id\":\"protein_sets_nb_specific_peptides\",\"title\":\"#specific_peptides\",\"default_displayed\":true},{\"id\":\"protein_sets_nb_peptide_matches\",\"title\":\"#peptide_matches\",\"default_displayed\":true},{\"id\":\"protein_sets_nb_specific_peptide_matches\",\"title\":\"#specific_peptide_matches\",\"default_displayed\":true}],\"default_displayed\":true},{\"id\":\"all_psm\",\"title\":\"all PSMs from protein sets\",\"presentation\":\"columns\",\"fields\":[{\"id\":\"psm_peptide_id\",\"title\":\"peptide_id\",\"default_displayed\":true},{\"id\":\"psm_sequence\",\"title\":\"sequence\",\"default_displayed\":true},{\"id\":\"psm_modifications\",\"title\":\"modifications\",\"default_displayed\":true},{\"id\":\"psm_score\",\"title\":\"psm_score\",\"default_displayed\":true},{\"id\":\"psm_calculated_mass\",\"title\":\"calculated_mass\",\"default_displayed\":true},{\"id\":\"psm_charge\",\"title\":\"charge\",\"default_displayed\":true},{\"id\":\"psm_experimental_moz\",\"title\":\"experimental_moz\",\"default_displayed\":true},{\"id\":\"psm_delta_moz\",\"title\":\"delta_moz\",\"default_displayed\":true},{\"id\":\"psm_rt\",\"title\":\"rt\",\"default_displayed\":true},{\"id\":\"psm_peptide_length\",\"title\":\"peptide_length\",\"default_displayed\":true},{\"id\":\"psm_initial_query_id\",\"title\":\"initial_query_id\",\"default_displayed\":true},{\"id\":\"psm_missed_cleavages\",\"title\":\"missed_cleavages\",\"default_displayed\":true},{\"id\":\"psm_rank\",\"title\":\"rank\",\"default_displayed\":true},{\"id\":\"psm_cd_pretty_rank\",\"title\":\"cd_pretty_rank\",\"default_displayed\":true},{\"id\":\"psm_fragment_matches_count\",\"title\":\"fragment_matches_count\",\"default_displayed\":true},{\"id\":\"psm_spectrum_title\",\"title\":\"spectrum_title\",\"default_displayed\":true},{\"id\":\"psm_nb_protein_sets\",\"title\":\"#protein_sets\",\"default_displayed\":true},{\"id\":\"psm_nb_protein_matches\",\"title\":\"#protein_matches\",\"default_displayed\":true},{\"id\":\"psm_nb_databank_protein_matches\",\"title\":\"#databank_protein_matches\",\"default_displayed\":true},{\"id\":\"psm_start\",\"title\":\"start\",\"default_displayed\":true},{\"id\":\"psm_end\",\"title\":\"end\",\"default_displayed\":true},{\"id\":\"psm_residue_before\",\"title\":\"residue_before\",\"default_displayed\":true},{\"id\":\"psm_residue_after\",\"title\":\"residue_after\",\"default_displayed\":true},{\"id\":\"protein_sets_id\",\"title\":\"protein_set_id\",\"default_displayed\":true},{\"id\":\"protein_sets_accession\",\"title\":\"accession\",\"default_displayed\":true},{\"id\":\"protein_sets_description\",\"title\":\"description\",\"default_displayed\":true},{\"id\":\"protein_sets_score\",\"title\":\"protein_set_score\",\"default_displayed\":true},{\"id\":\"protein_sets_is_validated\",\"title\":\"is_validated\",\"default_displayed\":true},{\"id\":\"protein_sets_selection_level\",\"title\":\"selection_level\",\"default_displayed\":false},{\"id\":\"protein_sets_nb_sameset_protein_matches\",\"title\":\"#sameset_protein_matches\",\"default_displayed\":false},{\"id\":\"protein_sets_nb_subset_protein_matches\",\"title\":\"#subset_protein_matches\",\"default_displayed\":false},{\"id\":\"protein_sets_coverage\",\"title\":\"coverage\",\"default_displayed\":false},{\"id\":\"protein_sets_mw\",\"title\":\"MW\",\"default_displayed\":false},{\"id\":\"protein_sets_nb_sequences\",\"title\":\"#sequences\",\"default_displayed\":false},{\"id\":\"protein_sets_nb_specific_sequences\",\"title\":\"#specific_sequences\",\"default_displayed\":false},{\"id\":\"protein_sets_nb_peptides\",\"title\":\"#peptides\",\"default_displayed\":false},{\"id\":\"protein_sets_nb_specific_peptides\",\"title\":\"#specific_peptides\",\"default_displayed\":false},{\"id\":\"protein_sets_nb_peptide_matches\",\"title\":\"#peptide_matches\",\"default_displayed\":false},{\"id\":\"protein_sets_nb_specific_peptide_matches\",\"title\":\"#specific_peptide_matches\",\"default_displayed\":false}],\"default_displayed\":false},{\"id\":\"stat\",\"title\":\"statistics\",\"presentation\":\"rows\",\"fields\":[{\"id\":\"stat_nb_protein_sets\",\"title\":\"#protein_sets\",\"default_displayed\":true},{\"id\":\"stat_psm_validation\",\"title\":\"psm_validation\",\"default_displayed\":true},{\"id\":\"stat_nb_total_precursors\",\"title\":\"#total_precursors\",\"default_displayed\":true},{\"id\":\"stat_nb_protein_sets_single_specific_peptide\",\"title\":\"#protein_sets_with_single_specific_peptide\",\"default_displayed\":true},{\"id\":\"stat_nb_modified_peptides\",\"title\":\"#modified_peptides\",\"default_displayed\":true},{\"id\":\"stat_nb_z3_precursors\",\"title\":\"#z3_precursors\",\"default_displayed\":true},{\"id\":\"stat_nb_unmodified_peptides\",\"title\":\"#unmodified_peptides\",\"default_displayed\":true},{\"id\":\"stat_nb_protein_sets_multi_specific_peptide\",\"title\":\"#protein_sets_with_multiple_specific_peptides\",\"default_displayed\":true},{\"id\":\"stat_nb_z2_precursors\",\"title\":\"#z2_precursors\",\"default_displayed\":true},{\"id\":\"stat_nb_peptides\",\"title\":\"#peptides\",\"default_displayed\":true},{\"id\":\"stat_nb_distinct_seq\",\"title\":\"#distinct_sequences\",\"default_displayed\":true},{\"id\":\"stat_prot_validation\",\"title\":\"prot_validation\",\"default_displayed\":true}],\"default_displayed\":true}],\"format_values\":[\"xlsx\",\"tsv\"],\"decimal_separator_values\":[\".\",\",\"],\"date_format_values\":[\"YYYY:MM:DD HH:mm:ss\",\"YYYY:MM:DD\"],\"title_separator_values\":[\"_\",\" \"],\"sheet_presentation_values\":[\"rows\",\"columns\"]}";
	        exportButton.addActionListener(new ActionListener() {

	            @Override
	            public void actionPerformed(ActionEvent e) {
	            	//msdiagDialog.setDefaultExportConfig(defaultConfig);
	            	msdiagDialog.setVisible(true);
	            }
	        });
	       // panel.add(exportButton, BorderLayout.NORTH);
	        //***
	        RsetMSDiagPanel msdiagPanel = new RsetMSDiagPanel("MSDiag panel");
            String jsonMessageHashMapJson; // json string that contains the data to be displayed
            //jsonMessageHashMapJson = "{\"Exp. MoZ per charge and score\":\"{\\\"matrix\\\":[[\\\"Unassigned for charge 1\\\",0.0,0.0,0.0,0.0],[\\\"Score <= 20.0 for charge 1\\\",0.0,0.0,0.0,0.0],[\\\"20.0 < score <= 40.0 for charge 1\\\",0.0,0.0,0.0,0.0],[\\\"40.0 < score <= 60.0 for charge 1\\\",0.0,0.0,0.0,0.0],[\\\"Score > 60.0 for charge 1\\\",0.0,0.0,0.0,0.0],[\\\"All assigned for charge 1\\\",0.0,0.0,0.0,0.0],[\\\"Unassigned for charge 2\\\",0.0,0.0,0.0,0.0],[\\\"Score <= 20.0 for charge 2\\\",302.17563,1485.32,587.7137040389031,556.772397],[\\\"20.0 < score <= 40.0 for charge 2\\\",309.647935,1346.157591,526.3222532827059,495.269083],[\\\"40.0 < score <= 60.0 for charge 2\\\",344.211425,1410.235107,558.0397207721358,539.762764],[\\\"Score > 60.0 for charge 2\\\",393.726978,1424.215114,702.7661301267062,701.343523],[\\\"All assigned for charge 2\\\",302.17563,1485.32,573.232238490797,543.7491485],[\\\"Unassigned for charge 3\\\",0.0,0.0,0.0,0.0],[\\\"Score <= 20.0 for charge 3\\\",300.165445,1541.50485,602.3940846636148,562.601455],[\\\"20.0 < score <= 40.0 for charge 3\\\",300.507365,1075.099971,476.7644315391989,424.903118],[\\\"40.0 < score <= 60.0 for charge 3\\\",300.506744,1057.897642,501.9996251619942,510.292244],[\\\"Score > 60.0 for charge 3\\\",367.868128,1184.273884,750.3526963181818,766.0313055],[\\\"All assigned for charge 3\\\",300.165445,1541.50485,584.259249608831,532.264279],[\\\"Unassigned for charge 4\\\",0.0,0.0,0.0,0.0],[\\\"Score <= 20.0 for charge 4\\\",307.928749,1534.309021,707.2920686498599,688.836248],[\\\"20.0 < score <= 40.0 for charge 4\\\",301.177419,959.496779,444.62825433823537,380.0937505],[\\\"40.0 < score <= 60.0 for charge 4\\\",0.0,0.0,0.0,0.0],[\\\"Score > 60.0 for charge 4\\\",567.057245,933.760247,811.5259666666667,933.760247],[\\\"All assigned for charge 4\\\",301.177419,1534.309021,699.4433728460846,687.8364799999999],[\\\"Unassigned for charge 5\\\",0.0,0.0,0.0,0.0],[\\\"Score <= 20.0 for charge 5\\\",493.881837,1168.815204,756.8069085291668,727.9994755],[\\\"20.0 < score <= 40.0 for charge 5\\\",539.066809,539.066809,539.066809,539.066809],[\\\"40.0 < score <= 60.0 for charge 5\\\",0.0,0.0,0.0,0.0],[\\\"Score > 60.0 for charge 5\\\",0.0,0.0,0.0,0.0],[\\\"All assigned for charge 5\\\",493.881837,1168.815204,756.3542264095636,727.999439],[\\\"Unassigned for charge 6\\\",0.0,0.0,0.0,0.0],[\\\"Score <= 20.0 for charge 6\\\",552.094171,974.181064,908.2619283553715,927.154301],[\\\"20.0 < score <= 40.0 for charge 6\\\",0.0,0.0,0.0,0.0],[\\\"40.0 < score <= 60.0 for charge 6\\\",0.0,0.0,0.0,0.0],[\\\"Score > 60.0 for charge 6\\\",0.0,0.0,0.0,0.0],[\\\"All assigned for charge 6\\\",552.094171,974.181064,908.2619283553715,927.154301],[\\\"Unassigned for charge 7\\\",0.0,0.0,0.0,0.0],[\\\"Score <= 20.0 for charge 7\\\",625.033696,1094.247935,795.7236974242423,797.132107],[\\\"20.0 < score <= 40.0 for charge 7\\\",0.0,0.0,0.0,0.0],[\\\"40.0 < score <= 60.0 for charge 7\\\",0.0,0.0,0.0,0.0],[\\\"Score > 60.0 for charge 7\\\",0.0,0.0,0.0,0.0],[\\\"All assigned for charge 7\\\",625.033696,1094.247935,795.7236974242423,797.132107],[\\\"Unassigned for charge 8\\\",0.0,0.0,0.0,0.0],[\\\"Score <= 20.0 for charge 8\\\",697.618441,697.618524,697.6184825,697.6184825],[\\\"20.0 < score <= 40.0 for charge 8\\\",0.0,0.0,0.0,0.0],[\\\"40.0 < score <= 60.0 for charge 8\\\",0.0,0.0,0.0,0.0],[\\\"Score > 60.0 for charge 8\\\",0.0,0.0,0.0,0.0],[\\\"All assigned for charge 8\\\",697.618441,697.618524,697.6184825,697.6184825]],\\\"output_type\\\":{\\\"enumClass\\\":\\\"fr.proline.module.quality.msdiag.msi.MSDiagOutputTypes\\\",\\\"value\\\":\\\"table\\\"},\\\"cell_type\\\":\\\"object scala.Double\\\",\\\"description\\\":\\\"Exp. MoZ per charge and score\\\",\\\"column_names\\\":[\\\"Charge & Score\\\",\\\"Lowest Mass\\\",\\\"Highest Mass\\\",\\\"Average Mass\\\",\\\"Median Mass\\\"],\\\"x_axis_description\\\":\\\"Masses\\\",\\\"y_axis_description\\\":\\\"Charges and scores\\\"}\",\"Score repartition for Target/Decoy data\":\"{\\\"matrix\\\":[[\\\"Target PSM\\\",22328,9105,3389,1927],[\\\"Decoy PSM\\\",20680,5790,614,42]],\\\"output_type\\\":{\\\"enumClass\\\":\\\"fr.proline.module.quality.msdiag.msi.MSDiagOutputTypes\\\",\\\"value\\\":\\\"table\\\"},\\\"cell_type\\\":\\\"object scala.Int\\\",\\\"description\\\":\\\"Score repartition for Target/Decoy data\\\",\\\"column_names\\\":[\\\"Resultset\\\",\\\"Score <= 20.0\\\",\\\"20.0 < score <= 40.0\\\",\\\"40.0 < score <= 60.0\\\",\\\"Score > 60.0\\\"],\\\"x_axis_description\\\":\\\"Scores\\\",\\\"y_axis_description\\\":\\\"Resultsets\\\"}\",\"Assigned and unassigned spectra\":\"{\\\"matrix\\\":[[8186,42295]],\\\"output_type\\\":{\\\"enumClass\\\":\\\"fr.proline.module.quality.msdiag.msi.MSDiagOutputTypes\\\",\\\"value\\\":\\\"pie\\\"},\\\"cell_type\\\":\\\"object scala.Int\\\",\\\"description\\\":\\\"Assigned and unassigned spectra\\\",\\\"column_names\\\":[\\\"Unassigned\\\",\\\"Assigned\\\"]}\",\"PSM per charge and score\":\"{\\\"matrix\\\":[[1,0,0,0,0,0],[2,6818,30098,13155,3682,1831],[3,1063,10152,1671,321,132],[4,209,2122,68,0,6],[5,86,480,1,0,0],[6,5,121,0,0,0],[7,2,33,0,0,0],[8,1,2,0,0,0]],\\\"output_type\\\":{\\\"enumClass\\\":\\\"fr.proline.module.quality.msdiag.msi.MSDiagOutputTypes\\\",\\\"value\\\":\\\"chromatogram\\\"},\\\"cell_type\\\":\\\"object scala.Int\\\",\\\"description\\\":\\\"PSM per charge and score\\\",\\\"column_names\\\":[\\\"Charge\\\",\\\"Unassigned\\\",\\\"Score <= 20.0\\\",\\\"20.0 < score <= 40.0\\\",\\\"40.0 < score <= 60.0\\\",\\\"Score > 60.0\\\"],\\\"x_axis_description\\\":\\\"Scores\\\",\\\"y_axis_description\\\":\\\"Charges\\\"}\",\"Number of matches per minute of retention time and score\":\"{\\\"matrix\\\":[[7,800,840,121,12,2],[8,1,3,3,0,0],[9,170,215,32,8,0],[10,901,2541,671,74,0],[11,1031,2804,1005,125,15],[12,658,2062,902,87,12],[13,522,2360,596,85,16],[14,679,2314,466,24,0],[15,1101,2146,214,4,1],[16,1306,1002,94,4,1],[17,616,144,0,0,0],[18,390,33,5,0,0],[19,11,3,2,0,0]],\\\"output_type\\\":{\\\"enumClass\\\":\\\"fr.proline.module.quality.msdiag.msi.MSDiagOutputTypes\\\",\\\"value\\\":\\\"chromatogram\\\"},\\\"cell_type\\\":\\\"object scala.Int\\\",\\\"description\\\":\\\"Number of matches per minute of retention time and score\\\",\\\"column_names\\\":[\\\"Retention time\\\",\\\"Unassigned\\\",\\\"Score <= 20.0\\\",\\\"20.0 < score <= 40.0\\\",\\\"40.0 < score <= 60.0\\\",\\\"Score > 60.0\\\"],\\\"x_axis_description\\\":\\\"Retention time\\\",\\\"y_axis_description\\\":\\\"Matches\\\"}\"}";
            jsonMessageHashMapJson =   "{\"Exp. MoZ per charge and score\":\"{\\\"matrix\\\":[[1,\\\"Unassigned\\\",200,300.0,140.0,150.0],[1,\\\"Score <= 20.0\\\",1244.62337,1244.62337,1244.62337,1244.62337],[1,\\\"20.0 < score <= 40.0\\\",0.0,0.0,0.0,0.0],[1,\\\"40.0 < score <= 60.0\\\",0.0,0.0,0.0,0.0],[1,\\\"Score > 60.0\\\",0.0,0.0,0.0,0.0],[1,\\\"All assigned\\\",1244.62337,1244.62337,1244.62337,1244.62337],[2,\\\"Unassigned\\\",0.0,0.0,0.0,0.0],[2,\\\"Score <= 20.0\\\",421.75782,1547.63084,906.2918965625,755.8920350000001],[2,\\\"20.0 < score <= 40.0\\\",508.29258,1275.58916,843.2025575,744.44299],[2,\\\"40.0 < score <= 60.0\\\",792.87844,792.87844,792.87844,792.87844],[2,\\\"Score > 60.0\\\",0.0,0.0,0.0,0.0],[2,\\\"All assigned\\\",421.75782,1547.63084,900.4049616190476,744.4435],[3,\\\"Unassigned\\\",0.0,0.0,0.0,0.0],[3,\\\"Score <= 20.0\\\",429.91182,1550.78804,971.7631210588239,869.07676],[3,\\\"20.0 < score <= 40.0\\\",570.93379,1531.71324,875.6735681818183,756.71932],[3,\\\"40.0 < score <= 60.0\\\",586.32447,1525.38028,1000.16274,944.473105],[3,\\\"Score > 60.0\\\",0.0,0.0,0.0,0.0],[3,\\\"All assigned\\\",429.91182,1550.78804,962.3292550000001,869.076505],[4,\\\"Unassigned\\\",0.0,0.0,0.0,0.0],[4,\\\"Score <= 20.0\\\",542.78068,1149.03172,857.7654329629628,844.5432800000001],[4,\\\"20.0 < score <= 40.0\\\",542.78085,1163.34028,982.6490379999999,1014.3608750000001],[4,\\\"40.0 < score <= 60.0\\\",1149.02964,1163.34028,1151.8919759999999,1149.03016],[4,\\\"Score > 60.0\\\",1144.28674,1144.28674,1144.28674,1144.28674],[4,\\\"All assigned\\\",542.78068,1163.34028,900.7081482857144,888.43131],[5,\\\"Unassigned\\\",0.0,0.0,0.0,0.0],[5,\\\"Score <= 20.0\\\",825.75949,915.62975,869.0777499999999,865.84401],[5,\\\"20.0 < score <= 40.0\\\",915.63067,915.63067,915.63067,915.63067],[5,\\\"40.0 < score <= 60.0\\\",0.0,0.0,0.0,0.0],[5,\\\"Score > 60.0\\\",0.0,0.0,0.0,0.0],[5,\\\"All assigned\\\",825.75949,915.63067,880.71598,890.7368799999999],[6,\\\"Unassigned\\\",0.0,0.0,0.0,0.0],[6,\\\"Score <= 20.0\\\",0.0,0.0,0.0,0.0],[6,\\\"20.0 < score <= 40.0\\\",0.0,0.0,0.0,0.0],[6,\\\"40.0 < score <= 60.0\\\",0.0,0.0,0.0,0.0],[6,\\\"Score > 60.0\\\",0.0,0.0,0.0,0.0],[6,\\\"All assigned\\\",0.0,0.0,0.0,0.0],[7,\\\"Unassigned\\\",0.0,0.0,0.0,0.0],[7,\\\"Score <= 20.0\\\",0.0,0.0,0.0,0.0],[7,\\\"20.0 < score <= 40.0\\\",0.0,0.0,0.0,0.0],[7,\\\"40.0 < score <= 60.0\\\",0.0,0.0,0.0,0.0],[7,\\\"Score > 60.0\\\",0.0,0.0,0.0,0.0],[7,\\\"All assigned\\\",0.0,0.0,0.0,0.0],[8,\\\"Unassigned\\\",0.0,0.0,0.0,0.0],[8,\\\"Score <= 20.0\\\",0.0,0.0,0.0,0.0],[8,\\\"20.0 < score <= 40.0\\\",0.0,0.0,0.0,0.0],[8,\\\"40.0 < score <= 60.0\\\",0.0,0.0,0.0,0.0],[8,\\\"Score > 60.0\\\",0.0,0.0,0.0,0.0],[8,\\\"All assigned\\\",0.0,0.0,0.0,0.0]],\\\"output_type\\\":{\\\"enumClass\\\":\\\"fr.proline.module.quality.msdiag.msi.MSDiagOutputTypes\\\",\\\"value\\\":\\\"box\\\"},\\\"cell_type\\\":\\\"object scala.Double\\\",\\\"description\\\":\\\"Exp. MoZ per charge and score\\\",\\\"column_names\\\":[\\\"Charge\\\",\\\"Score\\\",\\\"Lowest Mass\\\",\\\"Highest Mass\\\",\\\"Average Mass\\\",\\\"Median Mass\\\"],\\\"column_types\\\":[\\\"Integer\\\",\\\"String\\\",\\\"Double\\\",\\\"Double\\\",\\\"Double\\\",\\\"Double\\\"],\\\"column_categories\\\":[\\\"Category\\\",\\\"Category\\\",\\\"Data\\\",\\\"Data\\\",\\\"Data\\\",\\\"Data\\\"],\\\"x_axis_description\\\":\\\"Charge and score\\\",\\\"y_axis_description\\\":\\\"Mass\\\"}\",\"Score repartition for Target/Decoy data\":\"{\\\"matrix\\\":[[\\\"Target PSM\\\",194,30,10,1],[\\\"Decoy PSM\\\",46,0,0,0]],\\\"output_type\\\":{\\\"enumClass\\\":\\\"fr.proline.module.quality.msdiag.msi.MSDiagOutputTypes\\\",\\\"value\\\":\\\"categoryplot\\\"},\\\"cell_type\\\":\\\"object scala.Int\\\",\\\"description\\\":\\\"Score repartition for Target/Decoy data\\\",\\\"column_names\\\":[\\\"Resultset\\\",\\\"Score <= 20.0\\\",\\\"20.0 < score <= 40.0\\\",\\\"40.0 < score <= 60.0\\\",\\\"Score > 60.0\\\"],\\\"column_types\\\":[\\\"String\\\",\\\"Integer\\\",\\\"Integer\\\",\\\"Integer\\\",\\\"Integer\\\"],\\\"column_categories\\\":[\\\"Category\\\",\\\"Data\\\",\\\"Data\\\",\\\"Data\\\",\\\"Data\\\"],\\\"x_axis_description\\\":\\\"Scores\\\",\\\"y_axis_description\\\":\\\"Resultsets\\\"}\",\"Assigned and unassigned spectra\":\"{\\\"matrix\\\":[[4578,141]],\\\"output_type\\\":{\\\"enumClass\\\":\\\"fr.proline.module.quality.msdiag.msi.MSDiagOutputTypes\\\",\\\"value\\\":\\\"pie\\\"},\\\"cell_type\\\":\\\"object scala.Int\\\",\\\"description\\\":\\\"Assigned and unassigned spectra\\\",\\\"column_names\\\":[\\\"Unassigned\\\",\\\"Assigned\\\"],\\\"column_types\\\":[\\\"Double\\\",\\\"Double\\\"],\\\"column_categories\\\":[\\\"Data\\\",\\\"Data\\\"]}\",\"PSM per charge and score\":\"{\\\"matrix\\\":[[1,4011,2,0,0,0],[2,339,96,8,1,0],[3,139,85,11,4,0],[4,60,54,10,5,1],[5,22,3,1,0,0],[6,5,0,0,0,0],[7,1,0,0,0,0],[8,1,0,0,0,0]],\\\"output_type\\\":{\\\"enumClass\\\":\\\"fr.proline.module.quality.msdiag.msi.MSDiagOutputTypes\\\",\\\"value\\\":\\\"chromatogram\\\"},\\\"cell_type\\\":\\\"object scala.Int\\\",\\\"description\\\":\\\"PSM per charge and score\\\",\\\"column_names\\\":[\\\"Charge\\\",\\\"Unassigned\\\",\\\"Score <= 20.0\\\",\\\"20.0 < score <= 40.0\\\",\\\"40.0 < score <= 60.0\\\",\\\"Score > 60.0\\\"],\\\"column_types\\\":[\\\"Integer\\\",\\\"Integer\\\",\\\"Integer\\\",\\\"Integer\\\",\\\"Integer\\\",\\\"Integer\\\"],\\\"column_categories\\\":[\\\"Category\\\",\\\"Data\\\",\\\"Data\\\",\\\"Data\\\"],\\\"x_axis_description\\\":\\\"Charge\\\",\\\"y_axis_description\\\":\\\"PSMs\\\"}\",\"Number of matches per minute of retention time and score\":\"{\\\"matrix\\\":[[9,151,0,0,0,0],[10,144,0,0,0,0],[11,130,0,0,0,0],[12,102,2,1,0,0],[13,140,5,0,0,0],[14,135,1,0,0,0],[15,118,6,0,0,0],[16,118,5,0,0,0],[17,109,3,0,0,0],[18,117,12,1,0,0],[19,97,13,4,3,0],[20,110,2,0,0,0],[21,114,12,1,0,1],[22,154,8,2,1,0],[23,163,0,0,0,0],[24,186,0,0,0,0],[25,195,0,0,0,0],[26,205,0,0,0,0],[27,204,0,0,0,0],[28,208,0,0,0,0],[29,180,0,0,0,0],[30,129,0,0,0,0],[31,137,0,0,0,0],[32,138,0,0,0,0],[33,146,0,0,0,0],[34,145,0,0,0,0],[35,137,0,0,0,0],[36,136,0,0,0,0],[37,140,0,0,0,0],[38,130,0,0,0,0],[39,130,0,0,0,0],[40,130,0,0,0,0]],\\\"output_type\\\":{\\\"enumClass\\\":\\\"fr.proline.module.quality.msdiag.msi.MSDiagOutputTypes\\\",\\\"value\\\":\\\"chromatogram\\\"},\\\"cell_type\\\":\\\"object scala.Int\\\",\\\"description\\\":\\\"Number of matches per minute of retention time and score\\\",\\\"column_names\\\":[\\\"Retention time\\\",\\\"Unassigned\\\",\\\"Score <= 20.0\\\",\\\"20.0 < score <= 40.0\\\",\\\"40.0 < score <= 60.0\\\",\\\"Score > 60.0\\\"],\\\"column_types\\\":[\\\"Double\\\",\\\"Integer\\\",\\\"Integer\\\",\\\"Integer\\\",\\\"Integer\\\",\\\"Integer\\\"],\\\"column_categories\\\":[\\\"Category\\\",\\\"Data\\\",\\\"Data\\\",\\\"Data\\\",\\\"Data\\\",\\\"Data\\\"],\\\"x_axis_description\\\":\\\"Retention time\\\",\\\"y_axis_description\\\":\\\"Matches\\\"}\"}";

            
            msdiagPanel.setData(jsonMessageHashMapJson);
            msdiagPanel.setVisible(true);
            panel.add(msdiagPanel);
	        //***
            
            
            getContentPane().setLayout(new BorderLayout());
	        getContentPane().add(panel, BorderLayout.CENTER);
	        pack();
	        setSize(1200, 800);
	        setLocation(200, 100);
	        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

	    }

	    /**
	     * @param args the command line arguments
	     */
	    public static void main(String[] args) {
	        //try {
	            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
	                if ("Windows".equals(info.getName())) {
	                    try {
							UIManager.setLookAndFeel(info.getClassName());
						} catch (ClassNotFoundException
								| InstantiationException
								| IllegalAccessException
								| UnsupportedLookAndFeelException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                    break;
	                }
	            }
	            SwingUtilities.invokeLater(new Runnable() {

	            @Override
	            public void run() {
	                //CustomExportDialogDemo exportF = new CustomExportDialogDemo();
	            	MSDiagTest msdiagTest = new MSDiagTest();
	            	msdiagTest.setVisible(true);
	            	//MSDiagDialog msdiagDialog = new MSD
	                //msdiagDialog.setVisible(true);
	               
	            
	            }
	        });
	    }
	}


