/**
 * Hash table for storing strings and their frequencies using an array
 * of data items (string, frequency).
 * 
 * Hash table implemented from the ground up without using 
 * Java collections framework.
 * 
 * Hash table uses open addressing through linear probing 
 * and dynamically resizes when maximum allowable load factor is exceeded.
 * Load factor = (current item + deleted items) / total slots
 * Note that deleted items must be used for calculating 
 * load factor of linear probing hash table. 
 * 
 * Hash table also keeps track of number of collisions
 * between different keys. 
 *  
 * @author Sharif Doghmi
 */
public class MyHashTable implements MyHTInterface {

	/**
	 * Default capacity of hash table. 
	 */
	private final int DEFAULT_CAP = 10;
	
	/**
	 * Constant radix used in the hashing function. 
	 */
	private final int HASH_CONST = 27;
	
	/**
	 * Constant used in hashing function for mapping characters to 
	 * integer values by displacing their numerical values by a fixed amount.
	 * 
	 * For example, 'a' maps to 1. 
	 */
	private final int DISPLACEMENT = 96;
	
	/**
	 * Maximum allowed load factor of hash table after which table is resized 
	 * by at least doubling.
	 * 
	 * Current load factor is calculated by dividing number of occupied cells by 
	 * table length and compared to maximum load factor after every new item
	 * addition.
	 */
	private final double MAX_LOAD_FACTOR = 0.5;
	
	/**
	 * Hash Table data structure: array of data items.
	 */
	private DataItem[] HashTable;
	
	/**
	 * Number of active entries in the hash table. 
	 */
	private int numKeys;
	
	/**
	 * Number of active entries in the hash table plus number of deleted
	 * entries. 
	 * 
	 * These are considered occupied cells because they slow down
	 * the deletion and search process during linear probing. They must
	 * be kept track of for calculation of the load factor for resizing purposes.  
	 */
	private int numOccupied;
	
	/**
	 * Total number of key collisions in the hash table.
	 * 
	 * A collision is defined as two keys that map to the same hash value.
	 */
	private int numCollisions;

	/**
	 * Dummy object for all deleted data items. All deleted data items point to it. 
	 */
	private DataItem deletedItem;
	
	/**
	 * Constructs a hash table with the specified initial capacity.
	 * 
	 * @param size 		chosen initial hash table size
	 * 
	 */
	public MyHashTable(int size) {
		if (size < 1) 
			throw new IllegalArgumentException("Initial size cannot be less "
				+ "than one");
		HashTable = new DataItem[size];
		numCollisions = 0;
		numKeys = 0;
		numOccupied = 0;
		deletedItem = new DataItem("#DEL#", 0);
	}

	/**
	 * Constructs a hash table with default capacity as size.
	 */
	public MyHashTable() {
		HashTable = new DataItem[DEFAULT_CAP];
		numCollisions = 0;
		numKeys = 0;
		numOccupied = 0;
		deletedItem = new DataItem("#DEL#", 0);
	}
	
	@Override
	public void insert(String value) {
		
		// Check for illegal arguments
		if (value == null || value.length() < 1) return;
		
		int index = 0;
		int found = search(value);
		
		// if string does not already exist
		if (found == -1) {
			index = hashFunc(value);
			
			// Count collisions that new string makes with existing but different strings
			int newCollisions = countCollisions(value);
		
			// Loop until empty location is found
			while (HashTable[index] != null && HashTable[index] != deletedItem) {
				index++;
				index = index % HashTable.length;
			}
			HashTable[index] = new DataItem(value, 1);
			numCollisions += newCollisions;
			numKeys++;
			numOccupied++;
			
			// Rehash if load factor exceeded
			if ( ((double) (numOccupied) / (double) HashTable.length) > MAX_LOAD_FACTOR) rehash();
		
		// If string already exists
		} else {
			HashTable[found].incFreq();
		}
	}

	/**
	 * Counts number of collisions specified string makes with pre-existing 
	 * but different strings in the hash table.
	 * 
	 * @param value 	string for which to count collisions in hash table
	 * @return 			number of collisions in hash table with specified string
	 */
	private int countCollisions(String value) {

		// Check for illegal arguments
		if (value == null || value.length() < 1) return 0;
		
		int collCount = 0;
		int origHash = hashFunc(value);
		int index = origHash;
		
		// Loop counting collisions with existing but different keys
		while (HashTable[index] != null) {
			if (hashFunc(HashTable[index].getValue()) == origHash
					&& !value.equals(HashTable[index].getValue())) collCount++;
			index++;
			index = index % HashTable.length;
		}
		
		return collCount;
	}

	/**
	 * Searches for string key in hash table.
	 * 
	 * @param key		string to search hash table for
	 * @return 			index of key if found, -1 if not found
	 */
	private int search(String key) {
		
		// Check for illegal arguments
		if (key == null || key.length() < 1) return -1;
		
		int index = hashFunc(key);
		
		// Loop searching for key and return if found
		while (HashTable[index] != null) {
			if (HashTable[index].getValue().equals(key)) return index; //Key exists
			index++;
			index = index % HashTable.length;
		}
		
		return -1; // Key does not exist
	}
	
	@Override
	public int size() {
		return numKeys;
	}

	@Override
	public void display() {
		System.out.print("Table: ");
		for (int index = 0; index < HashTable.length; index++) {
			if (HashTable[index] == null) {
				System.out.print("** ");
			} else if (HashTable[index] == deletedItem) {
				System.out.print("#DEL# ");
			} else {
				System.out.print(HashTable[index] + " ");
			}
		}
		System.out.println();
	}

	@Override
	public boolean contains(String key) {

		// Check for illegal arguments
		if (key == null || key.length() < 1) return false;
		
		return (search(key) != -1);
	}

	@Override
	public int numOfCollisions() {
		return numCollisions;
	}

	@Override
	public int hashValue(String value) {
		
		// Check for illegal arguments
		if (value == null || value.length() < 1) return -1;
		
		return hashFunc(value);
	}

	@Override
	public int showFrequency(String key) {
		
		// Check for illegal arguments
		if (key == null || key.length() < 1) return 0;
		
		int index = search(key);
		if (index == -1) return 0; // key not found
		else return HashTable[index].getFrequency();
	}

	@Override
	public String remove(String key) {
		
		// Check for illegal arguments
		if (key == null || key.length() < 1) return null;
		
		int index = search(key);
		
		// If key does not exist, return null
		if (index == -1) return null;
		
		// Key exists with frequency > 1. No need to delete. Just decrement freq.
		if (HashTable[index].getFrequency() > 1) {
			HashTable[index].decFreq();
			return HashTable[index].getValue();
			
		// Key exists with frequency = 1. Entry must be deleted.	
		} else if((HashTable[index].getFrequency() == 1)) {
			
			// Check how many other keys it collided with, in order
			// to update collision count after deletion.
			int collCount = countCollisions(key);
			
			String tmp = HashTable[index].getValue();
			HashTable[index] = deletedItem;
			
			/*
			 *  Update current number of keys in hash table and current number
			 *  of collisions, but don't update number of occupied entries,
			 *  as the key was marked as deleted. Deleted keys slow down
			 *  the search and delete process, and are used in the calculation
			 *  of the load factor.
			 */
			numKeys--;
			numCollisions -= collCount;
			
			return tmp;
		}
		return null;
	}

	/**
	 * Calculates hash value of input string.
	 * 
	 * @param input		the string to calculate hash value for
	 * 
	 * @return 			hash value of specified string
	 */
	private int hashFunc(String input) {
		if (input.length() < 1) return 0;
		return hornerRecurse(input, input.length() - 1);
	}

	/**
	 * Helper method for hash function to calculate hash value recursively
	 * using Horner's method.
	 * 
	 * @param input		the string to calculate hash value for
	 * @param n			index position of string to use in this recursive call
	 * @return 			sum of recursive tree so far
	 */
	private int hornerRecurse(String input, int n) {

		// Base case: reached first character
		if (n == 0) return (toInt(input.charAt(0)) % HashTable.length); 
		
		// Recursive case:
		return (toInt(input.charAt(n)) + HASH_CONST*(hornerRecurse(input, n-1)) ) % HashTable.length;
	}

	/**
	 * Convert character to integer by subtracting a constant from it
	 * such that 'a' character maps to 1. Also, guarantees return of positive
	 * value to prevent negative indices for hash values.
	 * 
	 * @param c			the character to convert to integer
	 * @return			absolute integer value mapping for the specified character
	 */
	private int toInt(char c) {
		int ret = c - DISPLACEMENT;
		if (ret >=0) return ret;
		
		// If negative, return absolute value
		return -ret;
	}

	/**
	 * Doubles array length and rehashes items. Called when load factor is exceeded.
	 */
	private void rehash() {
		DataItem[] OldHashTable = HashTable;
		
		// Calculate new hash table size as first prime greater than double old size
		int newSize = getPrime(HashTable.length*2 + 1);
		
		System.out.println("Rehashing " + numKeys + " items, new size is " + newSize);
		HashTable = new DataItem[newSize];
		
		// Reset global variables
		numCollisions = 0;
		numKeys = 0;
		numOccupied = 0;
		
		// Copy all data items from old hash table to new hash table
		for (int i = 0; i < OldHashTable.length; i++) {
			if (OldHashTable[i] != null && OldHashTable[i] != deletedItem) {
				String oldKey = OldHashTable[i].getValue();
				for (int num = OldHashTable[i].getFrequency(); num > 0; num--) {
					insert(oldKey);
				}
			}
		}		
	}

	/**
	 * Returns first prime number greater than or equal to specified integer.
	 * 
	 * @param n		integer to find first prime number greater than or equal to
	 * @return		prime number found
	 */
	private int getPrime(int n) {
		while(true) {
			if (isPrime(n)) return n;
			n++;
		}
	}
	
	/**
	 * Checks primality of specified integer.
	 * 
	 * @param n		integer to check for primality
	 * @return		true if integer is prime, false if not
	 */
	private boolean isPrime(int n) {
		for (int i = 2; i <= n/2; i++) {
			if (n % i == 0) return false;
		}
		return true;
	}
	
	/**
	 * Data Item object stores individual entries in the hash table.
	 * 
	 * Each data item contains a string key and the frequency of that string,
	 * for example, in a parsed document.
	 * 
	 * @author Sharif Doghmi
	 * 
	 */
	private static class DataItem {
		private String value;
		private int frequency;

		/**
		 * Constructs data item using input string and frequency.
		 * 
		 * @param val		string to store
		 * @param freq		frequency of string
		 */
		public DataItem(String val, int freq) {
			value = val;
			frequency = freq;
		}
		
		/**
		 * @return	string stored in data item
		 */
		public String getValue() {
			return value;
		}
		
		/**
		 * @return 	frequency of stored string
		 */
		public int getFrequency() {
			return frequency;
		}

		public String toString() {
			return ("[" + getValue() +", " + getFrequency() + "]");
		}
		
		/**
		 * Increments frequency of this string.
		 */
		public void incFreq() {
			frequency++;
		}
		
		/**
		 * Decrements frequency of this string.
		 */
		public void decFreq() {
			frequency--;
		}
	}
}
