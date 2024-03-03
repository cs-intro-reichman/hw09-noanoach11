import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
        In in = new In(fileName);
        StringBuilder window = new StringBuilder();
        
        for (int i = 0; i < windowLength && !in.isEmpty(); i++) {
            char c = in.readChar();
            window.append(c);
        }
    
        // Read through the rest of the file
        while (!in.isEmpty()) {
            char nextChar = in.readChar();
            
            if (window.length() < windowLength) {
                window.append(nextChar);
            } else {
               
                String currentWindow = window.toString();
                List probs = CharDataMap.getOrDefault(currentWindow, new List());
                probs.update(nextChar);
                CharDataMap.put(currentWindow, probs);
    
                window.deleteCharAt(0);
                window.append(nextChar);
            }
        }
        
        // Calculate probabilities for all lists in the CharDataMap
        for (List list : CharDataMap.values()) {
            calculateProbabilities(list);
        }
    }
	

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {				
        double totalCount = 0;
        for (int i = 0; i <probs.getSize(); i++) {
            totalCount += probs.get(i).count;
        }
    
        double totalCp = 0.0;
        for (int i = 0; i < probs.getSize(); i++) {
            CharData charData = probs.get(i);
            charData.p = charData.count/totalCount;
            totalCp += charData.p;
            charData.cp = totalCp;
            }
        }  
	

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
        double randomValue = randomGenerator.nextDouble();
        ListIterator iterator = probs.listIterator(0);
        while (iterator.hasNext()) {
            CharData curreCharData = iterator.next();
            if (randomValue < curreCharData.cp) {
                return curreCharData.chr;
            }
        }
        return probs.get(probs.getSize()-1).chr;
    }
	

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
        StringBuilder generated = new StringBuilder(initialText);
        while (generated.length() < textLength) {
            String currentWindow = generated.substring(Math.max(0, generated.length() - windowLength));
            if (!CharDataMap.containsKey(currentWindow)) {
                break;
            }
            List probs = CharDataMap.get(currentWindow);
            char nextChar = getRandomChar(probs); 
            generated.append(nextChar); 
    
            if (generated.length() >= textLength) {
                break;
            }
        }
    
        // Ensure the generated string is trimmed to the exact desired length in case it exceeds it.
        if (generated.length() > textLength) {
            return generated.substring(0, textLength);
        } else {
            return generated.toString();
        }
    }

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
		// Your code goes here
    }
}
