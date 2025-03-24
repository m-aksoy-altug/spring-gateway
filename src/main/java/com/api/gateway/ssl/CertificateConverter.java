
package com.api.gateway.ssl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.Certificate;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import javax.management.RuntimeErrorException;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.RuntimeCryptoException;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



import io.micrometer.common.util.StringUtils;

@Component
public class CertificateConverter {

	private static final Logger log = LoggerFactory.getLogger(CertificateConverter.class);
	
	
	@Autowired 
	private SecureCredentialServiceClient secureCredentialServiceClient; 
	public static final String KEY_STORE_TYPE_PKCS12="PKCS12";
	public static final String UTF_8_CHARSET = "UTF-8";
	public static final String KEY_STORE_NAME_PREFIX = "MyKeyStore";
	public static final String PKCS12_KEY_STORE_NAME_SUFFIX = ".p12";
	public static final String KEY_ALIAS_PREFIX = "Certificate and Key";
	public static final String CERT_ALIAS_PREFIX = "Cert Authority Certificate";

	public static final String convertCertificateToPem(final Certificate certificate) {
		StringWriter pemCertWriter = new StringWriter();
		JcaPEMWriter pemWriter = new JcaPEMWriter(pemCertWriter);
		try {
			pemWriter.writeObject(certificate);
			pemWriter.close();
		} catch (Exception e) {
			throw new RuntimeException("Error writing certificate to PEM file");
		}
		return pemCertWriter.toString();
	}

	public static final String convertCertificateToUuencodedPem(final Certificate certificate) {
		String pemCertificate = convertCertificateToPem(certificate);
		return uuencodeString(pemCertificate);
	}

	public static final String convertPrivateKeyToPem(final Key privateKey) {
		StringWriter pemKeyStrtWriter = new StringWriter();
		JcaPEMWriter pemWriter = new JcaPEMWriter(pemKeyStrtWriter);
		try {
			pemWriter.writeObject(privateKey);
			pemWriter.close();
		} catch (Exception e) {
			throw new RuntimeException("Error writing a key to PEM file");
		}
		return pemKeyStrtWriter.toString();
	}

	public static final String convertPrivateKeyToUuencodedPem(final Key privateKey) {
		String pem = convertPrivateKeyToPem(privateKey);
		return uuencodeString(pem);
	}

	public static final X509Certificate convertUUencodedPemToX509Cert(final String uuencodedPEM)
			throws CertificateException {
		byte[] caByteArray = uudecodeByteArray(uuencodedPEM);
		InputStream caStream = new ByteArrayInputStream(caByteArray);
		X509Certificate x509Ca = (X509Certificate) CertificateFactory.getInstance("X.509")
				.generateCertificate(new BufferedInputStream(caStream));
		return x509Ca;
	}

	public static final byte[] uudecodeByteArray(final String uudecodedPEM) {
		return Base64.getDecoder().decode(uudecodedPEM);
	}

	public static final String uuencodeString(final String strPemCertificate) {
		byte[] uuencodePemCertificate = Base64.getEncoder()
				.encode(strPemCertificate.getBytes(Charset.forName(UTF_8_CHARSET)));
		return new String(uuencodePemCertificate, Charset.forName(UTF_8_CHARSET));
	}

	public static final PrivateKey convertUuencodedPemToPrivateKey(final String uuencodedPemPrivateKey) {
		PrivateKey decryptedPrivateKey; 
		byte[] pkbyteArray= CertificateConverter.uudecodeByteArray(uuencodedPemPrivateKey); 
		String pkString = new String(pkbyteArray,Charset.forName(UTF_8_CHARSET)); 
		try {
			PEMParser keyReader = new PEMParser(new StringReader(pkString));
			Object keyPair = keyReader.readObject();
			keyReader.close();	
			if(keyPair instanceof PrivateKeyInfo) {
				PrivateKeyInfo keyInfo = ((PrivateKeyInfo) keyPair);
				decryptedPrivateKey = (new JcaPEMKeyConverter()).getPrivateKey(keyInfo);
			}
			else if(keyPair instanceof PEMKeyPair) {
				PEMKeyPair pemKeypair = ((PEMKeyPair) keyPair);
				decryptedPrivateKey = (new JcaPEMKeyConverter()).getKeyPair(pemKeypair).getPrivate();
			}
			else {
				throw new RuntimeException("There was a problem coverting the ETCD value to a java private key");
			}		
		}catch(IOException e) {
			throw new RuntimeException("Error converting the string private key into a java Privatekey",e);
		}
		return decryptedPrivateKey;
	}
	
	public static final KeyStore createPKCS12KeyStore(final SslDetails sslDetails) {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		// Create a PKCS12 Keystore
		KeyStore keyStore;
		try {
			keyStore = KeyStore.getInstance(KEY_STORE_TYPE_PKCS12, "BC");
			keyStore.load(null, null);
		}catch(Exception e) {
			throw new RuntimeException("Error ccreating the KeyStore",e);
		}
		return keyStore;		
	}
	
	/*Create a PCKS12 key store load the ETCD details into it and write it a temp file
	 * that is deleted on exit.
	 * @param sslDetails, contains ca,cert and key
	 * @param keyStorePass, keystore access creds
	 * @param Path,path o created file 
	*/ 
	public final Path buildPKCS12KeyStoreTempPath(final SslDetails sslDetails, final String keyStorePass) {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		// Create a PKCS12 keyStore
		KeyStore keyStore = createPKCS12KeyStore(sslDetails);
		// Load the ETCD certificates chain and private key into it.
		loadCertificateAndKey(sslDetails, keyStore);
		// Write the PKCS12 Keysotre to disk
//		Path keyStorePath= writeKeyStoreToTempFile(CleanString.cleanString(keyStore,KEY_STORE_NAME_PREFIX),
//				PKCS12_KEY_STORE_NAME_SUFFIX,keyStorePass); // String formatting can cause problem!! . 
		Path keyStorePath= writeKeyStoreToTempFile(keyStore,KEY_STORE_NAME_PREFIX,
				PKCS12_KEY_STORE_NAME_SUFFIX,keyStorePass);
		return keyStorePath;
	}
	
	public static final Path writeKeyStoreToTempFile(final KeyStore keyStore,
			final String fileNamePrefix,
			final String fileNameExt,
			final String storePassword) {	
		Path keyStoreFile= null;
		try {
			keyStoreFile= Files.createTempFile(fileNamePrefix, fileNameExt);	
			keyStoreFile.toFile().deleteOnExit();
			try(FileOutputStream keyStoreFop = new FileOutputStream(keyStoreFile.toFile());){
				keyStore.store(keyStoreFop, storePassword.toCharArray());
			}catch(Exception e) {
				throw new RuntimeException("Error openning the file output stream for the certificate and key to the PKCS12 file!",e);			
			}
		}
		catch(Exception e) {
			throw new RuntimeException("Error writing the certificate and key to the PKCS12 file!",e);
		}
		
//		FileOutputStream keyStoreFop= null;
//		Path keyStoreFile= null;
//		try {
//			keyStoreFile= Files.createTempFile(fileNamePrefix, fileNameExt);	
//			keyStoreFile.toFile().deleteOnExit();
//			keyStoreFop = new FileOutputStream(keyStoreFile.toFile());
//			keyStore.store(keyStoreFop, storePassword.toCharArray());
//		}
//		catch(Exception e) {
//			throw new RuntimeException("Error writing the certificate and key to the PKCS12 file!",e);
//		}
//		finally {
//			IOUtils.closeQuietly(keyStoreFop);
//		}
		return keyStoreFile;	
	}
	
	public final void loadCertificateAndKey(final SslDetails sslDetails, final KeyStore keyStore) {
		boolean certificateFound= false;
		if(StringUtils.isNotBlank(sslDetails.getCert())) {
			certificateFound=true;
		}
		boolean keyFound= false;
		if(StringUtils.isNotBlank(sslDetails.getKey())) {
			keyFound=true;
		}
		if(certificateFound && keyFound) {
			loadCertificateAndPrivateKey(sslDetails,keyStore);
		}else if(certificateFound && !keyFound) {
			log.error("Certificate is set but the key was not in ETCD so not setting either in the key store");
		}else if(!certificateFound && keyFound) {
			log.error("Key is set but the certificate was not in ETCD so not setting either in the key store");
		}else {
			log.error("Key and Certificate were not in ETCD so not setting either in the key store");
		}
	}
	
	private void loadCertificateAndPrivateKey(SslDetails sslDetails, KeyStore keyStore) {
		try {
			log.debug("Adding the certificate chain");
			List<X509Certificate> caCertificate= new ArrayList<>();
			
			log.debug("Converting the certificate to the X509 format");
			X509Certificate x509Cert= CertificateConverter
					.convertUUencodedPemToX509Cert(sslDetails.getCert());
			caCertificate.add(x509Cert);	
//			if(CollectionUtils.isNotEmpty(sslDetails.getCA())) {	// org.apache.commons.collections4.CollectionUtils
			if(!sslDetails.getCA().isEmpty()) {	
				for(String caCertx: sslDetails.getCA()) {
					caCertificate.add(CertificateConverter.convertUUencodedPemToX509Cert(caCertx));
				}
			}else {
				log.debug("No CA found...");
			}
			if(StringUtils.isNotBlank(sslDetails.getKey())) {
				log.debug("Converting the key Private format");
				PrivateKey privateKey= CertificateConverter.convertUuencodedPemToPrivateKey(sslDetails.getKey());
				log.debug("Loading the certificatre and key in to the key store");	
				Certificate[] certArray = new Certificate[caCertificate.size()];
				caCertificate.toArray(certArray);
				String keyPassword= secureCredentialServiceClient.getCertificateDetails("keyStorePassword"); // returning dummy password
				// Same password is also used for KeyManagerFactory
				if(keyPassword !=null) {
					keyStore.setKeyEntry(KEY_ALIAS_PREFIX, privateKey, keyPassword.toCharArray(), 
							 certArray);
				}
			}else {
				log.debug("No Private key found...");	
			}
		}catch(Exception e) {
			throw new RuntimeException("Exception while creating the Certificate/Private Key entry in the key store",e);
		}		
	}
		
	public static final void loadCertificateAuthorities(final SslDetails sslDetails, final KeyStore keyStore) {
		int i=0;
		for(String stringCa: sslDetails.getCA()) {
			i++;
			log.debug("Loading certificate "+i+ "into the key store");
			try {
				X509Certificate x509Ca= CertificateConverter.convertUUencodedPemToX509Cert(stringCa);
				keyStore.setCertificateEntry(CERT_ALIAS_PREFIX+Integer.toString(i), x509Ca);
				log.debug("Loaded certificate ["+x509Ca.getSubjectDN()+ "] into the key store");
			}catch(Exception e) {
				log.debug("Exception occur while processing one of the certificates in the SSL configuration",e);
			}
			
		}
		
	}
}
