package cmd;
//BT3 MAPD Param Display by ViveTheJoestar
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class Main {
	private static final byte[] MAPD = { 0x4D, 0x41, 0x50, 0x44 };
	private static final String[] MAPD_PARAMS = {
		"Dust Color 1", "Dust Color 2", "Shadow Color", "UnkParam 1",
		"Dark BG Color", "Lighting Color", "UnkParam 2", "Random Color 1",
		"UnkParam 3", "UnkParam 4", "Random Color 2", "Random Color 3",
		"Random Color 4", "Random Color 5"
	};
	private static int getMapdAddr(RandomAccessFile pak) throws IOException {
		byte[] input = new byte[4];
		int addr = 0;
		pak.seek(0);
		int numEntries = LittleEndian.getInt(pak.readInt());
		if (numEntries != 25) return -1;
		pak.seek(8);
		addr = LittleEndian.getInt(pak.readInt());
		pak.seek(addr);
		pak.read(input);
		if (Arrays.equals(input, MAPD)) return addr;
		return -1;
	}
	private static void printMapParams(RandomAccessFile pak, int addr) throws IOException {
		pak.seek(addr + 116);
		int mapParamAddr = LittleEndian.getInt(pak.readInt());
		mapParamAddr += addr;
		pak.seek(mapParamAddr);
		for (int i = 0; i < MAPD_PARAMS.length; i++) {
			pak.seek(mapParamAddr+=4);
			int param = LittleEndian.getInt(pak.readInt());
			if (!MAPD_PARAMS[i].contains("Color")) System.out.printf("%14s: %15d\n", MAPD_PARAMS[i], param);
			else {
				byte[] colorBytes = LittleEndian.getByteArrayFromInt(LittleEndian.getInt(param));
				String colorByteText = "";
				for (int j = 0; j < 4; j++) colorByteText += String.format("%3d ", colorBytes[j] & 0xFF);
				System.out.printf("%14s: %s\n", MAPD_PARAMS[i], colorByteText);
			}
		}
	}
	public static void main(String[] args) {
		String helpText = "USAGE: java -jar mapd-param.jar \"path/to/pak/folder\"";
		File[] mapFiles = null;
		//check for arguments
		if (args.length > 0) {
			if (args[0].equals("-h")) System.out.println(helpText);
			else {
				File tmp = new File(args[0]);
				if (tmp.isDirectory()) {
					File[] tmpFiles = tmp.listFiles((dir, name) -> (name.toLowerCase().endsWith(".pak")));
					if (tmpFiles.length > 0) mapFiles = tmpFiles;
					else System.out.println("ERROR: Directory does NOT contain BT3 map files!");
				}
				else System.out.println("ERROR: Directory does NOT point to a file!");
			}
		}
		else {
			System.out.println(helpText);
			System.exit(0);
		}
		//proceed with the rest
		if (mapFiles != null && mapFiles.length > 0) {
			try {
				long start = System.currentTimeMillis();
				RandomAccessFile[] maps = new RandomAccessFile[mapFiles.length];
				for (int i = 0; i < maps.length; i++) {
					maps[i] = new RandomAccessFile(mapFiles[i], "rw");
					int addr = getMapdAddr(maps[i]);
					if (addr < 0) System.out.println("WARNING: Skipping " + mapFiles[i].getName() + "...");
					else {
						System.out.println("[" + mapFiles[i].getName() + "]");
						printMapParams(maps[i], addr);
						System.out.println();
					}
				}
				long end = System.currentTimeMillis();
				System.out.println("Time Elapsed: " + (end - start) / 1000.0 + " s");
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}