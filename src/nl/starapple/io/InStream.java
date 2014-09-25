// Copyright 2014 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//	
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package nl.starapple.io;

import java.io.IOException;
import java.io.InputStream;

public class InStream extends Thread {
	
	StringBuffer buffer;
	
	int bufIndex;
	
	InputStream in;
	
	Boolean stopping;

	public InStream(InputStream inputStream) {
		in = inputStream;
		buffer = new StringBuffer();
		bufIndex = 0;
		stopping = false;
	}
	
	String[] newLines = new String[] {
			"\r\n",
			"\n",
			"\r"
	};

	public String readLine(long timeout) {
		long timeStart = System.currentTimeMillis();
		synchronized( buffer ) {
			while( true ) {
				int nltype = 0, index = -1;
				while( nltype < newLines.length ) {
					index = buffer.indexOf(newLines[nltype], bufIndex);
					if( index >= 0 ) { break; }
					++nltype;
				}
				if( index < 0 ) {
					long timeNow = System.currentTimeMillis();
					long timeElapsed = timeNow - timeStart;
					if( timeElapsed >= timeout ) {
						return null;
					}
					try {
						buffer.wait(timeout - timeElapsed);
					} catch( InterruptedException e ) {
						// Nothing
					}
				} else {
					String line = buffer.substring(bufIndex, index);
					//buffer.delete(0, index + newLines[nltype].length());
					bufIndex = index + newLines[nltype].length();
					return line;
				}
			}
		}
	}

	@Override
	public void run() {
		try {
			while( true ) {
				synchronized( stopping ) {
					if( stopping ) {
						break;
					}
				}
				int ch = in.read();
				if( ch >= 0 ) {
					synchronized( buffer ) {
						buffer.append((char) ch);
						buffer.notify();
					}
				}
			}
		} catch( IOException e ) {
	        synchronized( stopping ) {
                if( stopping ) {
                    return;
                }
            }
	        e.printStackTrace();
		}
	}

	public void finish() {
		synchronized( stopping ) {
			stopping = true;
		}
	}
	
	public String getData() {
		synchronized( buffer ) {
			return buffer.toString();
		}
	}

}
