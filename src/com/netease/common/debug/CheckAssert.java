package com.netease.common.debug;

/**
 * assert 断言处理
 * 
 * @author dingding
 *
 */
public class CheckAssert {

	private static final boolean ASSERT_ENABLE = false;
	
	private CheckAssert() {
		
	}
	
	public static void checkNull(String string) {
		if (ASSERT_ENABLE) {
			if (string == null) {
				throw new AssertionError("str null");
			}
		}
	}
	
	public static void checkNotNull(String string) {
		if (ASSERT_ENABLE) {
			if (string != null) {
				throw new AssertionError("str not null");
			}
		}
	}
	
	public static void checkNull(Object object) {
		if (ASSERT_ENABLE) {
			if (object == null) {
				throw new AssertionError("obj null");
			}
		}
	}
	
	public static void checkNotNull(Object object) {
		if (ASSERT_ENABLE) {
			if (object != null) {
				throw new AssertionError("obj not null");
			}
		}
	}
	
	/**
	 * 
	 * @param var
	 * @param op '='（"=="）、'>'、'<'
	 * @param value
	 */
	public static void checkValue(byte var, char op, byte value) {
		if (ASSERT_ENABLE) {
			switch (op) {
			case '>':
				if (var > value) {
					throw new AssertionError("byte var > value");
				}
				break;
			case '=':
				if (var == value) {
					throw new AssertionError("byte var == value");
				}
				break;
			case '<':
				if (var < value) {
					throw new AssertionError("byte var < value");
				}
				break;
			}
		}
	}
	
	/**
	 * 
	 * @param var
	 * @param op "<="、">="、"!="、"=="、">"、"<"
	 * @param value
	 */
	public static void checkValue(byte var, String op, byte value) {
		if (ASSERT_ENABLE) {
			String intern = op.intern();
			if (">=" == intern) {
				if (var >= value) {
					throw new AssertionError("byte var >= value");
				}
			} else if ("<=" == intern) {
				if (var <= value) {
					throw new AssertionError("byte var <= value");
				}
			} else if ("!=" == intern) {
				if (var != value) {
					throw new AssertionError("byte var != value");
				}
			} else if ("==" == intern) {
				if (var == value) {
					throw new AssertionError("byte var == value");
				}
			} else if (">" == intern) {
				if (var > value) {
					throw new AssertionError("byte var > value");
				}
			} else if ("<" == intern) {
				if (var < value) {
					throw new AssertionError("byte var < value");
				}
			}
		}
	}
	
	/**
	 * 
	 * @param var
	 * @param op '='（"=="）、'>'、'<'
	 * @param value
	 */
	public static void checkValue(short var, char op, short value) {
		if (ASSERT_ENABLE) {
			switch (op) {
			case '>':
				if (var > value) {
					throw new AssertionError("short var > value");
				}
				break;
			case '=':
				if (var == value) {
					throw new AssertionError("short var == value");
				}
				break;
			case '<':
				if (var < value) {
					throw new AssertionError("short var < value");
				}
				break;
			}
		}
	}
	
	/**
	 * 
	 * @param var
	 * @param op "<="、">="、"!="、">"、"<"
	 * @param value
	 */
	public static void checkValue(short var, String op, short value) {
		if (ASSERT_ENABLE) {
			String intern = op.intern();
			if (">=" == intern) {
				if (var >= value) {
					throw new AssertionError("short var >= value");
				}
			} else if ("<=" == intern) {
				if (var <= value) {
					throw new AssertionError("short var <= value");
				}
			} else if ("!=" == intern) {
				if (var != value) {
					throw new AssertionError("short var != value");
				}
			} else if ("==" == intern) {
				if (var == value) {
					throw new AssertionError("short var == value");
				}
			} else if (">" == intern) {
				if (var > value) {
					throw new AssertionError("short var > value");
				}
			} else if ("<" == intern) {
				if (var < value) {
					throw new AssertionError("short var < value");
				}
			}
		}
	}
	
	/**
	 * 
	 * @param var
	 * @param op '='（"=="）、'>'、'<'
	 * @param value
	 */
	public static void checkValue(int var, char op, int value) {
		if (ASSERT_ENABLE) {
			switch (op) {
			case '>':
				if (var > value) {
					throw new AssertionError("int var > value");
				}
				break;
			case '=':
				if (var == value) {
					throw new AssertionError("int var == value");
				}
				break;
			case '<':
				if (var < value) {
					throw new AssertionError("int var < value");
				}
				break;
			}
		}
	}
	
	/**
	 * 
	 * @param var
	 * @param op "<="、">="、"!="、">"、"<"
	 * @param value
	 */
	public static void checkValue(int var, String op, int value) {
		if (ASSERT_ENABLE) {
			String intern = op.intern();
			if (">=" == intern) {
				if (var >= value) {
					throw new AssertionError("int var >= value");
				}
			} else if ("<=" == intern) {
				if (var <= value) {
					throw new AssertionError("int var <= value");
				}
			} else if ("!=" == intern) {
				if (var != value) {
					throw new AssertionError("int var != value");
				}
			} else if ("==" == intern) {
				if (var == value) {
					throw new AssertionError("int var == value");
				}
			} else if (">" == intern) {
				if (var > value) {
					throw new AssertionError("int var > value");
				}
			} else if ("<" == intern) {
				if (var < value) {
					throw new AssertionError("int var < value");
				}
			}
		}
	}
	
	/**
	 * 
	 * @param var
	 * @param op '='（"=="）、'>'、'<'
	 * @param value
	 */
	public static void checkValue(long var, char op, long value) {
		if (ASSERT_ENABLE) {
			switch (op) {
			case '>':
				if (var > value) {
					throw new AssertionError("long var > value");
				}
				break;
			case '=':
				if (var == value) {
					throw new AssertionError("long var == value");
				}
				break;
			case '<':
				if (var < value) {
					throw new AssertionError("long var < value");
				}
				break;
			}
		}
	}
	
	/**
	 * 
	 * @param var
	 * @param op "<="、">="、"!="、"=="、">"、"<"
	 * @param value
	 */
	public static void checkValue(long var, String op, long value) {
		if (ASSERT_ENABLE) {
			String intern = op.intern();
			if (">=" == intern) {
				if (var >= value) {
					throw new AssertionError("long var >= value");
				}
			} else if ("<=" == intern) {
				if (var <= value) {
					throw new AssertionError("long var <= value");
				}
			} else if ("!=" == intern) {
				if (var != value) {
					throw new AssertionError("long var != value");
				}
			} else if ("==" == intern) {
				if (var == value) {
					throw new AssertionError("long var == value");
				}
			} else if (">" == intern) {
				if (var > value) {
					throw new AssertionError("long var > value");
				}
			} else if ("<" == intern) {
				if (var < value) {
					throw new AssertionError("long var < value");
				}
			}
		}
	}
	
	/**
	 * 
	 * @param var
	 * @param op '='（"=="）、'>'、'<'
	 * @param value
	 */
	public static void checkValue(float var, char op, float value) {
		if (ASSERT_ENABLE) {
			switch (op) {
			case '>':
				if (var > value) {
					throw new AssertionError("float var > value");
				}
				break;
			case '=':
				if (var == value) {
					throw new AssertionError("float var == value");
				}
				break;
			case '<':
				if (var < value) {
					throw new AssertionError("float var < value");
				}
				break;
			}
		}
	}
	
	/**
	 * 
	 * @param var
	 * @param op "<="、">="、"!="、"=="、">"、"<"
	 * @param value
	 */
	public static void checkValue(float var, String op, float value) {
		if (ASSERT_ENABLE) {
			String intern = op.intern();
			if (">=" == intern) {
				if (var >= value) {
					throw new AssertionError("float var >= value");
				}
			} else if ("<=" == intern) {
				if (var <= value) {
					throw new AssertionError("float var <= value");
				}
			} else if ("!=" == intern) {
				if (var != value) {
					throw new AssertionError("float var != value");
				}
			} else if ("==" == intern) {
				if (var == value) {
					throw new AssertionError("float var == value");
				}
			} else if (">" == intern) {
				if (var > value) {
					throw new AssertionError("float var > value");
				}
			} else if ("<" == intern) {
				if (var < value) {
					throw new AssertionError("float var < value");
				}
			}
		}
	}
	
	/**
	 * 
	 * @param var
	 * @param op '='（"=="）、'>'、'<'
	 * @param value
	 */
	public static void checkValue(double var, char op, double value) {
		if (ASSERT_ENABLE) {
			switch (op) {
			case '>':
				if (var > value) {
					throw new AssertionError("double var > value");
				}
				break;
			case '=':
				if (var == value) {
					throw new AssertionError("double var == value");
				}
				break;
			case '<':
				if (var < value) {
					throw new AssertionError("double var < value");
				}
				break;
			}
		}
	}
	
	/**
	 * 
	 * @param var
	 * @param op "<="、">="、"!="、"=="、">"、"<"
	 * @param value
	 */
	public static void checkValue(double var, String op, double value) {
		if (ASSERT_ENABLE) {
			String intern = op.intern();
			if (">=" == intern) {
				if (var >= value) {
					throw new AssertionError("double var >= value");
				}
			} else if ("<=" == intern) {
				if (var <= value) {
					throw new AssertionError("double var <= value");
				}
			} else if ("!=" == intern) {
				if (var != value) {
					throw new AssertionError("double var != value");
				}
			} else if ("==" == intern) {
				if (var == value) {
					throw new AssertionError("double var == value");
				}
			} else if (">" == intern) {
				if (var > value) {
					throw new AssertionError("double var > value");
				}
			} else if ("<" == intern) {
				if (var < value) {
					throw new AssertionError("double var < value");
				}
			}
		}
	}
}
