import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.JsonAdapter;
import com.hdactech.command.HdacCommand;
import com.hdactech.command.HdacException;
import com.hdactech.object.KeyPairs;
import com.hdactech.object.formatters.GsonFormatters;
import com.hdactech.object.queryobjects.AssetQuantity;

public class Sample {

	public static void main(String[] args) throws HdacException {
		HdacCommand cmd = new HdacCommand("191.1.11.125", "5756", "hdacrpc", "hdac1234");
		// HdacCommand cmd = new HdacCommand("192.168.181.130", "6448", "hdacrpc",
		// "hdac1234");

		List<KeyPairs> keyPairs = cmd.getAddressCommand().createKeyPairs(2);

		String address_1 = keyPairs.get(0).getAddress();
		String pubkey_1 = keyPairs.get(0).getPubkey();
		String privkey_1 = keyPairs.get(0).getPrivkey();

		String address_2 = keyPairs.get(1).getAddress();
		String pubkey_2 = keyPairs.get(1).getPubkey();
		String privkey_2 = keyPairs.get(1).getPrivkey();

		System.out.println("Address_1 : " + address_1);
		System.out.println("Public key_1 : " + pubkey_1);
		System.out.println("Private key_1 : " + privkey_1);

		System.out.println("Address_2 : " + address_2);
		System.out.println("Public key_2 : " + pubkey_2);
		System.out.println("Private key_2 : " + privkey_2);

		String contractHash = getFileHash("halla-01-001.txt");
		String signature_1 = cmd.getMessagingCommand().signMessage(privkey_1, contractHash);
		String signature_2 = cmd.getMessagingCommand().signMessage(privkey_2, contractHash);
		System.out.println("signature with privkey_1 : " + signature_1);
		System.out.println("signature with privkey_2 : " + signature_2);

		/*
		 * boolean verify = cmd.getMessagingCommand().verifyMessage(address_1,
		 * signature_1, txtHash); System.out.println("result : " + verify);
		 */

		JsonArray signArray = new JsonArray();
		signArray.add(signature_1);
		signArray.add(signature_2);

		JsonObject contractObject = new JsonObject();
		contractObject.addProperty("ContractHash", contractHash);
		contractObject.add("Signature", signArray);
		String strContractObject = contractObject.toString();
		System.out.println("strContractObject : " + strContractObject);
		String hexstrContractObject = "";
		for (int i = 0; i < strContractObject.length(); i++) {
			hexstrContractObject += String.format("%02X", (int) strContractObject.charAt(i));
		}
		System.out.println("hexstrContractObject : " + hexstrContractObject);

		String txid = cmd.getStreamCommand().publish("halla", "halla-01-001", hexstrContractObject);
		System.out.println("Result txid : " + txid);

		String hexContractData = cmd.getStreamCommand().listStreamItems("halla").get(0).getData();
		System.out.println("hexContractData : " + hexContractData);

		JsonParser parser = new JsonParser();
		JsonObject jsonObject = parser.parse(hexContractData).getAsJsonObject();

		String strContractHash = jsonObject.get("ContractHash").getAsString();
		System.out.println("Contract Hash : " + strContractHash);

		/*
		 * byte[] bytesContractData = DatatypeConverter.parseHexBinary(hexContractData);
		 * String strContractData = null; try { strContractData = new
		 * String(bytesContractData, "UTF-8"); } catch (UnsupportedEncodingException e)
		 * { // TODO Auto-generated catch block e.printStackTrace(); }
		 * System.out.println("strContractData : " + strContractData); Gson gson = new
		 * Gson(); Contract result = gson.fromJson(strContractData, Contract.class);
		 * System.out.println(result);
		 */
	}

	public class Contract extends GsonFormatters {

		String ContractHash = null;

		public Contract(String ContractHash) {
			super();
			this.ContractHash = ContractHash;
		}

		private String getContractHash() {
			return ContractHash;
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
		final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}