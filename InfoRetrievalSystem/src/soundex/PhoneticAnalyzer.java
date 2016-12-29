package soundex;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.phonetic.*;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.commons.codec.*;
import org.apache.commons.codec.language.RefinedSoundex;

public class PhoneticAnalyzer extends Analyzer {

    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;    
    public static final Encoder DEFAULT_PHONETIC_ENCODER = new RefinedSoundex();

    private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;
    private Encoder encoder = DEFAULT_PHONETIC_ENCODER;

    public void setEncoder(Encoder encoder) {
        this.encoder = encoder;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final StandardTokenizer src = new StandardTokenizer();
        src.setMaxTokenLength(maxTokenLength);
        
        TokenStream tok = new StandardFilter(src);
        tok = new PhoneticFilter(tok, encoder, true); // store phonetic code
        return new TokenStreamComponents(src, tok);
    }
}
