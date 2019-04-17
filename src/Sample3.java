import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.bouncycastle.crypto.digests.SHA256Digest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hdactech.command.HdacCommand;
import com.hdactech.command.HdacException;
import com.hdactech.object.StreamKeyItem;

public class Sample3 {

	private final static String A_address = "1a9G4bW3ZTucMwMw6CKGPKTjabY4Qk2KZo8iGg";
	private final static String B_address = "1b6wzk2CQyKfpnJNeVGGX5u69f78jaZ51cqFtR";

	public static void main(String[] args) throws HdacException, JsonSyntaxException, UnsupportedEncodingException {

		HdacCommand cmd = new HdacCommand("191.1.11.125", "5756", "hdacrpc", "hdac1234");

		String contractHash = getFileHash("halla-01-001.txt");

		List<StreamKeyItem> streamkeyItem = cmd.getStreamCommand().listStreamKeyItems("e-contract", "halla-01-001");

		String key = streamkeyItem.get(streamkeyItem.size() - 1).getKey();
		String data = streamkeyItem.get(streamkeyItem.size() - 1).getData();

		// JsonData Parsing
		JsonParser jParser = new JsonParser();
		JsonObject jObj = jParser.parse(hexToString(data)).getAsJsonObject();

		String filehash = jObj.get("filehash").getAsString();
		String signA = jObj.get("sign-A").getAsString();
		String signB = jObj.get("sign-B").getAsString();

		System.out.println("filehash : " + filehash);
		System.out.println("sign-A : " + signA);
		System.out.println("sign-B : " + signB);

		// 계약서 검증
		if (contractHash.equals(filehash)) {
			System.out.println("계약서가 변조 되지 않았습니다.");
		} else {
			System.out.println("계약서가 변조 되었습니다.");
		}

		// 서명 검증
		boolean verifyA = cmd.getMessagingCommand().verifyMessage(A_address, signA, contractHash);
		boolean verifyB = cmd.getMessagingCommand().verifyMessage(B_address, signB, contractHash);

		if (verifyA) {
			System.out.println("a의 서명 검증에 성공하였습니다.");
		} else {
			System.out.println("a의 서명 검증에 실패하였습니다.");
		}

		if (verifyB) {
			System.out.println("B의 서명 검증에 성공하였습니다.");
		} else {
			System.out.println("B의 서명 검증에 실패하였습니다.");
		}
	}

	private static String getFileHash(String filePath) {

		File file = new File(filePath);
		Scanner scan = null;
		try {
			scan = new Scanner(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		List<Byte> bFile = new ArrayList<>();

		StringBuffer sb = new StringBuffer();
		while (scan.hasNext()) {
			sb.append(scan.nextLine());
		}

		byte[] docHash = sha256(sb.toString().getBytes());

		return bytesToHex(docHash);
	}

	private static byte[] sha256(byte[] buf) {
		SHA256Digest digest = new SHA256Digest();
		byte[] byteData = new byte[digest.getDigestSize()];
		digest.update(buf, 0, buf.length);
		digest.doFinal(byteData, 0);

		return byteData;
	}


	public static String bytesToHex(byte[] bytes) {
		final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
	
	private static String hexToString(String strHex) throws UnsupportedEncodingException {
		byte[] b = new byte[strHex.length() / 2];
	    for (int i = 0; i < b.length; i++) {
	      int index = i * 2;
	      int v = Integer.parseInt(strHex.substring(index, index + 2), 16);
	      b[i] = (byte) v;
	    }
	 
		return new String(b, "UTF-8");
	}
}
