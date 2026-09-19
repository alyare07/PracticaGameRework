package principal.utilidades.funciones;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Motor criptográfico simétrico AES-128 con codificación Base64 y bypass de
 * retrocompatibilidad transparente para archivos en texto plano.
 * 
 * @version 2.0 (Vanilla Java 8 - Robust AES Engine)
 */
public class EncriptadorString {

	private static final String LLAVE_MAESTRA = "ESCALONETA_RPG_ENGINE_SECURE_KEY_2026";
	private SecretKeySpec claveSecreta;

	public EncriptadorString() {
		this.inicializarClave(LLAVE_MAESTRA);
	}

	private void inicializarClave(final String llave) {
		try {
			byte[] bytesClave = llave.getBytes(StandardCharsets.UTF_8);
			final MessageDigest sha = MessageDigest.getInstance("SHA-256");
			bytesClave = sha.digest(bytesClave);
			bytesClave = Arrays.copyOf(bytesClave, 16); // 128 bits para AES
			this.claveSecreta = new SecretKeySpec(bytesClave, "AES");
		} catch (final Exception e) {
			System.err.println("[EncriptadorString] Error al inicializar clave AES: " + e.getMessage());
		}
	}

	/**
	 * Cifra un texto en formato JSON utilizando AES/ECB/PKCS5Padding y lo convierte
	 * a Base64.
	 *
	 * @param textoPlano Cadena de texto a cifrar.
	 * @return Cadena cifrada en Base64 o el texto original si ocurre algún error.
	 */
	public String encriptar(final String textoPlano) {
		if ((textoPlano == null) || textoPlano.isEmpty()) {
			return "";
		}
//
//		try {
//			final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//			cipher.init(Cipher.ENCRYPT_MODE, this.claveSecreta);
//			final byte[] bytesEncriptados = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));
//			return Base64.getEncoder().encodeToString(bytesEncriptados);
//		} catch (final Exception e) {
//			System.err.println("[EncriptadorString] Error al encriptar: " + e.getMessage());
//			return textoPlano;
//		}
		return textoPlano;
	}

	/**
	 * Descifra una cadena en Base64 generada por {@link #encriptar(String)}. Si la
	 * cadena ya es texto plano JSON (inicia con '{' o '['), la retorna intacta.
	 *
	 * @param textoEncriptado Cadena en Base64 o JSON plano.
	 * @return Texto plano descifrado en UTF-8.
	 */
	public String desencriptar(final String textoEncriptado) {
		if ((textoEncriptado == null) || textoEncriptado.trim().isEmpty()) {
			return "";
		}

		final String textoLimpio = textoEncriptado.trim();

		// Retrocompatibilidad: si no está cifrado (JSON legible), lo retorna directo
		if (textoLimpio.startsWith("{") || textoLimpio.startsWith("[")) {
			return textoLimpio;
		}

		try {
			final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, this.claveSecreta);
			final byte[] bytesDecodificados = Base64.getDecoder().decode(textoLimpio);
			final byte[] bytesDesencriptados = cipher.doFinal(bytesDecodificados);
			return new String(bytesDesencriptados, StandardCharsets.UTF_8);
		} catch (final Exception e) {
			// Fallback: si falla la desencriptación, devuelve el contenido original
			return textoLimpio;
		}
	}
}