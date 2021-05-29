import java.util.*;
import java.io.File; 
class Rename{
	
	// scan for -help command, if found, exit program and ignore all other options
	// return false if -h option is found and program must terminate
	public static Boolean scan_help (String args[]){
		int args_len = args.length; 
		for (int i = 0; i < args_len; i++){
			if (args[i].equals("-h") || args[i].equals("-help")){
				System.out.println("(c) 2021 Lily Zhang. Revised: May 27, 2021."); 
				System.out.println("Usage: rename [-option argument1 argument2 ...]"); 
				System.out.println("Options:"); 
				System.out.println("-f|file [filename]		:: file(s) to change."); 
				System.out.println("p|prefix [string]		:: rename [filename] so that it starts with [string].");
				System.out.println("-s|suffix [string]		:: rename [filename] so that it ends with [string].");
				System.out.println("-r|replace [str1] [str2]	:: rename [filename] by replacing all instances of [str1] with [str2].");
				System.out.println("-h|help 			:: print out this help and exit the program.");
				return false; 
			}
		}
		return true; 
	}

	public static String simplify_option(String s){
		if (s.equals("-f") || s.equals("-file")) {
		       return "f"; 
	      	} else if (s.equals("-p") || s.equals("-prefix")) {
		       return "p"; 
		} else if (s.equals("-s") || s.equals("-suffix")) {
		       return "s"; 
	      	} else if (s.equals("-r") || s.equals("replace")) {
		       return "r"; 
	      	} else {
	 		return "N"; // return None
		}
	}		

	public static Boolean is_option_raw_args(String s) {
		String ss = simplify_option(s); 
		return ss.equals("f") || ss.equals("p") || ss.equals("s")
		       	|| ss.equals("r"); 
	}


	public static Boolean is_option(String ss) {
		return ss.equals("f") || ss.equals("p") || ss.equals("s")
		       	|| ss.equals("r"); 
	}	
	
	// returns false if input has errors and program must terminate
	public static Boolean process_args(String args[], HashMap<String, String> files, ArrayList<String> prefix, 
			ArrayList<String> suffix, ArrayList <String> replace, ArrayList <String> order) {
		// contains fields f, p, s, r, h
		HashMap<String, Boolean> input_args = new HashMap<String, Boolean>();
		// each input can only be used onced
		input_args.put("f", false); 
		input_args.put("p", false); 
		input_args.put("s", false); 
		input_args.put("r", false); 
		int args_len = args.length; 
		int args_loc = 0; 
		String input; 
		while (args_loc < args_len) {
			input = simplify_option(args[args_loc]); 
			if (is_option(input) && input_args.get(input) == false){
				input_args.put(input, true); 
			} else if (is_option(input)){
				System.out.println("Option " + args[args_loc] + " specificed more than once"); 
				return false; 
			} else {
				System.out.println("Invalid option " + args[args_loc] + " specificed"); 
				return false; 
			}
			// read in all files
			if (input.equals("f")) {
				// skip over -f
				args_loc++;
				while ((args_loc < args_len) && !is_option_raw_args(args[args_loc])){
					// add to files
					files.put(args[args_loc], args[args_loc]); 
					args_loc++; 
				}
				args_loc--; // correct last off-by-one 
			} else if (input.equals("p")) {
				order.add("p"); 
				args_loc++; // skip over -p
				while((args_loc < args_len) && !is_option_raw_args(args[args_loc])){
					prefix.add(args[args_loc]); 
					args_loc++;
				}
				args_loc--; 
			} else if (input.equals("s")){
				order.add("s");
				args_loc++; // skip over -s
				while((args_loc < args_len) && !is_option_raw_args(args[args_loc])){
					suffix.add(args[args_loc]); 
					args_loc++;  
				}
				args_loc--; 
			} else if (input.equals("r")){
				order.add("r");
				args_loc++; // skip over -r
				int count = 0; 
				while((count < 2) && (args_loc < args_len) && !is_option_raw_args(args[args_loc])){
					replace.add(args[args_loc]); 
					args_loc++;
				        count++; 
				}
				if (count < 2) {
					System.out.println("Expected 2 arguments to -replace (-r) but found less"); 
					return false; 
				}
				args_loc--; 
			} else { // input == "N"
				System.out.println("Invalid option specified"); 
				return false; 
			}

			args_loc++; 
		}
		
		return true; 
	}

	public static void main (String args[]){
		HashMap<String, String> files = new HashMap<String, String>(); 
		ArrayList<String> suffix = new ArrayList<String>(); 
		ArrayList<String> prefix = new ArrayList<String>(); 
		ArrayList<String> replace = new ArrayList<String>(); 
		ArrayList<String> order = new ArrayList<String>(); 	
		// check input errors
		if(!scan_help(args) || !process_args(args, files, prefix, suffix, replace, order)) {
			return; 
		}
		File temp;
	        Boolean file_not_found = false; 	
		String cur_file_name; 
		// check if files exist
		for (Map.Entry f: files.entrySet()) {
			cur_file_name = (String) f.getKey(); 
			temp = new File(cur_file_name); 
			if (!temp.exists()) {
				System.out.println(cur_file_name + " not found"); 
				file_not_found = true;  
			}
		}
		// print all missing files then break
		if (file_not_found) {
			return;  
		}
		String new_file_name; 
		for (String option: order) {
			if (option.equals("s")) {
				for (String cur_suffix: suffix) {
					for (Map.Entry f: files.entrySet()) {
						cur_file_name = (String) f.getKey();
						new_file_name = (String) f.getValue() + cur_suffix; 
						files.put(cur_file_name, new_file_name); 
					}
				}
			} else if (option.equals ("p")) {
				for (String cur_prefix: prefix) {
					for (Map.Entry f: files.entrySet()) {
						cur_file_name = (String) f.getKey();
						new_file_name = cur_prefix + (String) f.getValue(); 
						files.put(cur_file_name, new_file_name); 
					}
				}
			} else { // option is replace
				String match_str = replace.get(0);
				String replace_str = replace.get(1); 
				for (Map.Entry f: files.entrySet()) {
					cur_file_name = (String) f.getKey();
					new_file_name = (String) f.getValue();
					if (!new_file_name.contains(match_str)) {
						System.out.println(new_file_name + "(converted from original name " +
								cur_file_name + ") does not have substring " + match_str + " for replacement"); 
					} else {
						new_file_name = new_file_name.replaceAll(match_str, replace_str);  
						files.put(cur_file_name, new_file_name); 
					}	
				}
			}
		}
		File source, dest; 
		for (Map.Entry f: files.entrySet()) {
			cur_file_name = (String) f.getKey();
		        new_file_name = (String) f.getValue();
		        source = new File (cur_file_name); 
			dest = new File(new_file_name); 	
			source.renameTo(dest); 
			System.out.println (cur_file_name + " --> " + new_file_name); 
		}
	}
}
