package com.diffbot.wikistatsextractor.extractors;

import org.apache.commons.cli.*;
import org.dbpedia.spotlight.db.model.TextTokenizer;
import org.dbpedia.spotlight.db.model.TextTokenizer$class;
import org.dbpedia.spotlight.db.stem.SnowballStemmer;
import org.dbpedia.spotlight.db.tokenize.LanguageIndependentTokenizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class Launcher {
	
	public static void main(String[] args) throws ParseException {

		Options options = new Options();
		options.addOption("tmp_folder", true, "");
		options.addOption("output_folder", true, "");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);
		List<String> appArgs = cmd.getArgList();

		String tmp_folder = cmd.getOptionValue("tmp_folder", "data/tmp");
		String output_folder = cmd.getOptionValue("output_folder", "data/output/");

		if (!tmp_folder.endsWith("/")) tmp_folder+="/";
		if (!output_folder.endsWith("/")) output_folder+="/";

		String language = appArgs.get(0);
		String locale = appArgs.get(1);
		String stemmer = appArgs.get(2);
		String dump_file=appArgs.get(3);
		String path_to_stopwords=appArgs.get(4);

		long start=System.currentTimeMillis();
		
		/** set the parameters */
		ExtractSFAndRedirections.LANGUAGE=language;

		/** extract all the surface forms, URI and redirections in the dump */
		ExtractSFAndRedirections.extractAllSurfaceFormsAndRedirection(dump_file, 
				output_folder+"pairCounts",
				tmp_folder+"tmp_redirections",
				output_folder+"uriCounts",
				tmp_folder+"tmp_surface_form_counts");
		
		ExtractAllNGrams.LOCALE=locale;
		ExtractAllNGrams.LANGUAGE=language;
		/** extract the ngrams: compute the number of time a surface form is a link compared to the number of time it 
		 *  is just a word  */
		ExtractAllNGrams.extractAllNGrams(dump_file,
				tmp_folder+"tmp_surface_form_counts",
				output_folder+"sfAndTotalCounts");
		
		/** The longest (and most incomprehensible) step. 
		 *  For each resource, extract  the surrounding token */
		ExtractContextualToken.LANGUAGE=language;

		ExtractContextualToken.extractContextualToken(dump_file,
				tmp_folder,
				output_folder + "tokenCounts",
				output_folder + "uriCounts",
				tmp_folder + "tmp_redirections",
				org.dbpedia.spotlight.db.tokenize.TextTokenizer.fromParameters(new Locale(locale), stemmer, path_to_stopwords, "", null)
		);
		
		
		System.out.println("all done in "+((System.currentTimeMillis()-start)/1000)+" seconds");
		
	}

}
