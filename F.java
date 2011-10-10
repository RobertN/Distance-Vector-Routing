class F {
  final static String SPACES = "                                                
                                                    ";
    public static String format(String s, int len){
	int slen = len-s.length();

	if(slen > SPACES.length())
	    slen = SPACES.length();
  
	if(slen > 0)
	    return SPACES.substring(0,slen)+s;
	else
	    return s;

    }

    public static String format(Object x, int len){
	return format(String.valueOf(x), len);
    }

    public static String format(long x, int len){
	return format(String.valueOf(x), len);
    }

    public static String format(double x, int len){
	return format(String.valueOf(x), len);
    }

    public static String format(char x, int len){
	return format(String.valueOf(x), len);
    }
}
