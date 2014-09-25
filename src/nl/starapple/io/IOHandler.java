package nl.starapple.io;

import java.io.IOException;

public class IOHandler {

	Process child;
	InStream out, err;
	OutStream in;
	
	public IOHandler(String command) throws IOException {
		child = Runtime.getRuntime().exec(command);
		in = new OutStream(child.getOutputStream());
		out = new InStream(child.getInputStream());
		err = new InStream(child.getErrorStream());
		out.start(); err.start();
	}
	
	public void stop() {
		try {
			in.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
		}
		child.destroy();
		out.finish();
		err.finish();
		if( out.isAlive() || err.isAlive() ) {
			if( out.isAlive() ) { out.interrupt(); }
			if( err.isAlive() ) { err.interrupt(); }
		}
		try {
			child.waitFor();
			out.join(110);
			err.join(110);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public String readLine(long timeout) {
		if( !isRunning() ) { return null; }
		try {
			in.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return out.readLine(timeout);
	}
	
	public boolean writeLine(String line) {
		if( !isRunning() ) { return false; }
		try {
			in.writeLine(line.trim());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public String getStdin() {
		return in.getData();
	}
	
	public String getStdout() {
		return out.getData();
	}
	
	public String getStderr() {
		return err.getData();
	}
	
	public boolean isRunning() {
		try {
			child.exitValue();
			return false;
		} catch( IllegalThreadStateException ex ) {
			return true;
		}
	}
}
