/**
 * 
 */
package org.topicquests.newasr;

import org.topicquests.newasr.api.IAsrDataProvider;
import org.topicquests.newasr.api.IAsrModel;
import org.topicquests.newasr.api.IDictionary;
import org.topicquests.newasr.api.IDictionaryClient;
import org.topicquests.newasr.dictionary.DictionaryHttpClient;
import org.topicquests.newasr.dictionary.DictionaryClient;
import org.topicquests.newasr.impl.ASRModel;
import org.topicquests.newasr.impl.PostgresWordGramGraphProvider;
import org.topicquests.newasr.pred.PredicateAssembler;
import org.topicquests.newasr.wg.WordGramUtil;
import org.topicquests.pg.PostgresConnectionFactory;
import org.topicquests.support.RootEnvironment;

/**
 * @author jackpark
 *
 */
public class ASREnvironment extends RootEnvironment {
	private PostgresConnectionFactory dbDriver = null;
	private IDictionaryClient dictionarHttpyClient;
	private IDictionary dictionary;
	private IAsrModel model;
	private IAsrDataProvider database;
	private WordGramUtil wgUtil;
	private PredicateAssembler predAssem;
	/**
	 * 
	 */
	public ASREnvironment() {
		super("asr-sentence-config.xml", "logger.properties");
		String schemaName = getStringProperty("DatabaseSchema");
		String dbName = getStringProperty("DatabaseName");
		dbDriver = new PostgresConnectionFactory(dbName, schemaName);
		dictionarHttpyClient = new DictionaryHttpClient(this);
		dictionary = new DictionaryClient(this);
		database = new PostgresWordGramGraphProvider(this);
		model = new ASRModel(this);
		wgUtil = new WordGramUtil(this);
		// firing up WordGramUtil bootstraps punctuation wordgram if not already there
//		booter = new BootstrapEngine(this);
//		predImporter = new PredicateImporter(this);
	}
	
	public IAsrDataProvider getDatabase() {
		return database;
	}
	public IDictionaryClient getDictionaryClient() {
		return dictionarHttpyClient;
	}
	public IDictionary getDictionary() {
		return dictionary;
	}
	public IAsrModel getModel() {
		return model;
	}
	public PostgresConnectionFactory getDatabaseDriver() {
		return dbDriver;
	}
	
	@Override
	public void shutDown() {
		System.out.println("Shutting down");

	}

}
