/*
 *  Copyright (c) 2018 Absa Bank Limited, All Rights Reserved.
 *
 *  This code is confidential to Absa Bank Limited and shall not be disclosed
 *  outside the Bank without the prior written permission of the Absa Legal
 *
 *  In the event that such disclosure is permitted the code shall not be copied
 *  or distributed other than on a need-to-know basis and any recipients may be
 *  required to sign a confidentiality undertaking in favor of Absa Bank
 *  Limited
 *
 */
package za.co.absa.openbankingapi;

import com.awfs.coordination.BuildConfig;

import java.io.IOException;
import java.util.Locale;


/**
 * <p>Encodes and decodes to and from Base64 notation.</p>
 *
 * <p>Example:</p>
 *
 * <code>String encoded = Base64.encode( myByteArray );</code>
 * <br />
 * <code>byte[] myByteArray = Base64.decode( encoded );</code>
 *
 * <p>The <tt>options</tt> parameter, which appears in a few places, is used to pass
 * several pieces of information to the encoder. In the "higher level" methods such as
 * encodeBytes( bytes, options ) the options parameter can be used to indicate such
 * things as first gzipping the bytes before encoding them, not inserting linefeeds,
 * and encoding using the URL-safe and Ordered dialects.</p>
 *
 */
public class Base64 {

    /** The Constant NO_OPTIONS. */
    private final static int NO_OPTIONS = 0;

    /** The Constant ENCODE. */
    private final static int ENCODE = 1;

    /** The Constant DECODE. */
    private final static int DECODE = 0;

    /** The Constant GZIP. */
    private final static int GZIP = 2;

    /** The Constant DONT_GUNZIP. */
    private final static int DONT_GUNZIP = 4;

    /** The Constant DO_BREAK_LINES. */
    private final static int DO_BREAK_LINES = 8;

    /** The Constant URL_SAFE. */
    public final static int URL_SAFE = 16;

    /** The Constant ORDERED. */
    private final static int ORDERED = 32;

    /*  ******** P R I V A T E F I E L D S ******** */

    /** The Constant MAX_LINE_LENGTH. */
    private final static int MAX_LINE_LENGTH = 76;

    /** The Constant EQUALS_SIGN. */
    private final static byte EQUALS_SIGN = (byte)'=';

    /** The Constant NEW_LINE. */
    private final static byte NEW_LINE = (byte)'\n';

    /** The Constant PREFERRED_ENCODING. */
    private final static String PREFERRED_ENCODING = "US-ASCII";

    /** The Constant WHITE_SPACE_ENC. */
    private final static byte WHITE_SPACE_ENC = -5; // Indicates white space in

    // encoding
    /** The Constant EQUALS_SIGN_ENC. */
    private final static byte EQUALS_SIGN_ENC = -1; // Indicates equals sign in

    // encoding

    /*  ******** S T A N D A R D B A S E 6 4 A L P H A B E T ******** */

    /** The Constant _STANDARD_ALPHABET. */
    /*
     * Host platform me be something funny like EBCDIC, so we hardcode these
     * values.
     */
    private final static byte[] _STANDARD_ALPHABET = {
            (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F', (byte)'G', (byte)'H',
            (byte)'I', (byte)'J', (byte)'K', (byte)'L', (byte)'M', (byte)'N', (byte)'O', (byte)'P',
            (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U', (byte)'V', (byte)'W', (byte)'X',
            (byte)'Y', (byte)'Z', (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f',
            (byte)'g', (byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l', (byte)'m', (byte)'n',
            (byte)'o', (byte)'p', (byte)'q', (byte)'r', (byte)'s', (byte)'t', (byte)'u', (byte)'v',
            (byte)'w', (byte)'x', (byte)'y', (byte)'z', (byte)'0', (byte)'1', (byte)'2', (byte)'3',
            (byte)'4', (byte)'5', (byte)'6', (byte)'7', (byte)'8', (byte)'9', (byte)'+', (byte)'/'
    };

    /** The Constant _STANDARD_DECODABET. */
    private final static byte[] _STANDARD_DECODABET = {
            -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 0 - 8
            -5, -5, // Whitespace: Tab and Linefeed
            -9, -9, // Decimal 11 - 12
            -5, // Whitespace: Carriage Return
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 14 -
            // 26
            -9, -9, -9, -9, -9, // Decimal 27 - 31
            -5, // Whitespace: Space
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 33 - 42
            62, // Plus sign at decimal 43
            -9, -9, -9, // Decimal 44 - 46
            63, // Slash at decimal 47
            52, 53, 54, 55, 56, 57, 58, 59, 60, 61, // Numbers zero through nine
            -9, -9, -9, // Decimal 58 - 60
            -1, // Equals sign at decimal 61
            -9, -9, -9, // Decimal 62 - 64
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, // Letters 'A' through
            // 'N'
            14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, // Letters 'O'
            // through 'Z'
            -9, -9, -9, -9, -9, -9, // Decimal 91 - 96
            26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, // Letters 'a'
            // through 'm'
            39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, // Letters 'n'
            // through 'z'
            -9, -9, -9, -9, -9 // Decimal 123 - 127
            , -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 128 -
            // 139
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 140 -
            // 152
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 153 -
            // 165
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 166 -
            // 178
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 179 -
            // 191
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 192 -
            // 204
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 205 -
            // 217
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 218 -
            // 230
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 231 -
            // 243
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9
            // Decimal 244 - 255
    };

    /*  ******** U R L S A F E B A S E 6 4 A L P H A B E T ******** */

    /** The Constant _URL_SAFE_ALPHABET. */
    private final static byte[] _URL_SAFE_ALPHABET = {
            (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F', (byte)'G', (byte)'H',
            (byte)'I', (byte)'J', (byte)'K', (byte)'L', (byte)'M', (byte)'N', (byte)'O', (byte)'P',
            (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U', (byte)'V', (byte)'W', (byte)'X',
            (byte)'Y', (byte)'Z', (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f',
            (byte)'g', (byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l', (byte)'m', (byte)'n',
            (byte)'o', (byte)'p', (byte)'q', (byte)'r', (byte)'s', (byte)'t', (byte)'u', (byte)'v',
            (byte)'w', (byte)'x', (byte)'y', (byte)'z', (byte)'0', (byte)'1', (byte)'2', (byte)'3',
            (byte)'4', (byte)'5', (byte)'6', (byte)'7', (byte)'8', (byte)'9', (byte)'-', (byte)'_'
    };

    /** The Constant _URL_SAFE_DECODABET. */
    private final static byte[] _URL_SAFE_DECODABET = {
            -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 0 - 8
            -5, -5, // Whitespace: Tab and Linefeed
            -9, -9, // Decimal 11 - 12
            -5, // Whitespace: Carriage Return
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 14 -
            // 26
            -9, -9, -9, -9, -9, // Decimal 27 - 31
            -5, // Whitespace: Space
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 33 - 42
            -9, // Plus sign at decimal 43
            -9, // Decimal 44
            62, // Minus sign at decimal 45
            -9, // Decimal 46
            -9, // Slash at decimal 47
            52, 53, 54, 55, 56, 57, 58, 59, 60, 61, // Numbers zero through nine
            -9, -9, -9, // Decimal 58 - 60
            -1, // Equals sign at decimal 61
            -9, -9, -9, // Decimal 62 - 64
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, // Letters 'A' through
            // 'N'
            14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, // Letters 'O'
            // through 'Z'
            -9, -9, -9, -9, // Decimal 91 - 94
            63, // Underscore at decimal 95
            -9, // Decimal 96
            26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, // Letters 'a'
            // through 'm'
            39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, // Letters 'n'
            // through 'z'
            -9, -9, -9, -9, -9 // Decimal 123 - 127
            , -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 128 -
            // 139
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 140 -
            // 152
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 153 -
            // 165
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 166 -
            // 178
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 179 -
            // 191
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 192 -
            // 204
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 205 -
            // 217
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 218 -
            // 230
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 231 -
            // 243
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9
            // Decimal 244 - 255
    };

    /*  ******** O R D E R E D B A S E 6 4 A L P H A B E T ******** */

    /** The Constant _ORDERED_ALPHABET. */
    private final static byte[] _ORDERED_ALPHABET = {
            (byte)'-', (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5', (byte)'6',
            (byte)'7', (byte)'8', (byte)'9', (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E',
            (byte)'F', (byte)'G', (byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L', (byte)'M',
            (byte)'N', (byte)'O', (byte)'P', (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U',
            (byte)'V', (byte)'W', (byte)'X', (byte)'Y', (byte)'Z', (byte)'_', (byte)'a', (byte)'b',
            (byte)'c', (byte)'d', (byte)'e', (byte)'f', (byte)'g', (byte)'h', (byte)'i', (byte)'j',
            (byte)'k', (byte)'l', (byte)'m', (byte)'n', (byte)'o', (byte)'p', (byte)'q', (byte)'r',
            (byte)'s', (byte)'t', (byte)'u', (byte)'v', (byte)'w', (byte)'x', (byte)'y', (byte)'z'
    };

    /** The Constant _ORDERED_DECODABET. */
    private final static byte[] _ORDERED_DECODABET = {
            -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 0 - 8
            -5, -5, // Whitespace: Tab and Linefeed
            -9, -9, // Decimal 11 - 12
            -5, // Whitespace: Carriage Return
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 14 -
            // 26
            -9, -9, -9, -9, -9, // Decimal 27 - 31
            -5, // Whitespace: Space
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 33 - 42
            -9, // Plus sign at decimal 43
            -9, // Decimal 44
            0, // Minus sign at decimal 45
            -9, // Decimal 46
            -9, // Slash at decimal 47
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, // Numbers zero through nine
            -9, -9, -9, // Decimal 58 - 60
            -1, // Equals sign at decimal 61
            -9, -9, -9, // Decimal 62 - 64
            11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, // Letters 'A'
            // through 'M'
            24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, // Letters 'N'
            // through 'Z'
            -9, -9, -9, -9, // Decimal 91 - 94
            37, // Underscore at decimal 95
            -9, // Decimal 96
            38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, // Letters 'a'
            // through 'm'
            51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, // Letters 'n'
            // through 'z'
            -9, -9, -9, -9, -9 // Decimal 123 - 127
            , -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 128
            // - 139
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 140 -
            // 152
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 153 -
            // 165
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 166 -
            // 178
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 179 -
            // 191
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 192 -
            // 204
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 205 -
            // 217
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 218 -
            // 230
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 231 -
            // 243
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9
            // Decimal 244 - 255
    };

    /*  ******** D E T E R M I N E W H I C H A L H A B E T ******** */

    /**
     * Gets the alphabet.
     *
     * @param options the options
     * @return the alphabet
     */
    private static byte[] getAlphabet(int options) {
        if ((options & URL_SAFE) == URL_SAFE)
            return _URL_SAFE_ALPHABET;
        else if ((options & ORDERED) == ORDERED)
            return _ORDERED_ALPHABET;
        else
            return _STANDARD_ALPHABET;
    } // end getAlphabet

    /**
     * Gets the decodabet.
     *
     * @param options the options
     * @return the decodabet
     */
    private static byte[] getDecodabet(int options) {
        if ((options & URL_SAFE) == URL_SAFE)
            return _URL_SAFE_DECODABET;
        else if ((options & ORDERED) == ORDERED)
            return _ORDERED_DECODABET;
        else
            return _STANDARD_DECODABET;
    } // end getAlphabet

    /**
     * Instantiates a new base64.
     */
    private Base64() {
    }

    /*  ******** E N C O D I N G M E T H O D S ******** */

    /**
     * Encode3to4.
     *
     * @param b4 the b4
     * @param threeBytes the three bytes
     * @param numSigBytes the num sig bytes
     * @param options the options
     * @return the byte[]
     */
    private static byte[] encode3to4(byte[] b4, byte[] threeBytes, int numSigBytes, int options) {
        encode3to4(threeBytes, 0, numSigBytes, b4, 0, options);
        return b4;
    } // end encode3to4

    /**
     * Encode3to4.
     *
     * @param source the source
     * @param srcOffset the src offset
     * @param numSigBytes the num sig bytes
     * @param destination the destination
     * @param destOffset the dest offset
     * @param options the options
     * @return the byte[]
     */
    private static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes,
                                     byte[] destination, int destOffset, int options) {

        final byte[] ALPHABET = getAlphabet(options);

        // 1 2 3
        // 01234567890123456789012345678901 Bit position
        // --------000000001111111122222222 Array position from threeBytes
        // --------| || || || | Six bit groups to index ALPHABET
        // >>18 >>12 >> 6 >> 0 Right shift necessary
        // 0x3f 0x3f 0x3f Additional AND

        // Create buffer with zero-padding if there are only one or two
        // significant bytes passed in the array.
        // We have to shift left 24 in order to flush out the 1's that appear
        // when Java treats a value as negative that is cast from a byte to an
        // int.
        final int inBuff = (numSigBytes > 0 ? ((source[srcOffset] << 24) >>> 8) : 0)
                | (numSigBytes > 1 ? ((source[srcOffset + 1] << 24) >>> 16) : 0)
                | (numSigBytes > 2 ? ((source[srcOffset + 2] << 24) >>> 24) : 0);

        switch (numSigBytes) {
            case 3:
                destination[destOffset] = ALPHABET[(inBuff >>> 18)];
                destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
                destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
                destination[destOffset + 3] = ALPHABET[(inBuff) & 0x3f];
                return destination;

            case 2:
                destination[destOffset] = ALPHABET[(inBuff >>> 18)];
                destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
                destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
                destination[destOffset + 3] = EQUALS_SIGN;
                return destination;

            case 1:
                destination[destOffset] = ALPHABET[(inBuff >>> 18)];
                destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
                destination[destOffset + 2] = EQUALS_SIGN;
                destination[destOffset + 3] = EQUALS_SIGN;
                return destination;

            default:
                return destination;
        } // end switch
    } // end encode3to4

    /**
     * Encode.
     *
     * @param raw the raw
     * @param encoded the encoded
     */
    public static void encode(java.nio.ByteBuffer raw, java.nio.ByteBuffer encoded) {
        final byte[] raw3 = new byte[3];
        final byte[] enc4 = new byte[4];

        while (raw.hasRemaining()) {
            final int rem = Math.min(3, raw.remaining());
            raw.get(raw3, 0, rem);
            Base64.encode3to4(enc4, raw3, rem, Base64.NO_OPTIONS);
            encoded.put(enc4);
        } // end input remaining
    }

    /**
     * Encode.
     *
     * @param raw the raw
     * @param encoded the encoded
     */
    public static void encode(java.nio.ByteBuffer raw, java.nio.CharBuffer encoded) {
        final byte[] raw3 = new byte[3];
        final byte[] enc4 = new byte[4];

        while (raw.hasRemaining()) {
            final int rem = Math.min(3, raw.remaining());
            raw.get(raw3, 0, rem);
            Base64.encode3to4(enc4, raw3, rem, Base64.NO_OPTIONS);
            for (int i = 0; i < 4; i++) {
                encoded.put((char)(enc4[i] & 0xFF));
            }
        } // end input remaining
    }

    /**
     * Encode object.
     *
     * @param serializableObject the serializable object
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String encodeObject(java.io.Serializable serializableObject)
            throws IOException {
        return encodeObject(serializableObject, NO_OPTIONS);
    } // end encodeObject

    /**
     * Encode object.
     *
     * @param serializableObject the serializable object
     * @param options the options
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static String encodeObject(java.io.Serializable serializableObject, int options)
            throws IOException {

        if (serializableObject == null)
            throw new NullPointerException("Cannot serialize a null object.");

        // Streams
        java.io.ByteArrayOutputStream baos = null;
        java.io.OutputStream b64os = null;
        java.util.zip.GZIPOutputStream gzos = null;
        java.io.ObjectOutputStream oos = null;

        try {
            // ObjectOutputStream -> (GZIP) -> Base64 -> ByteArrayOutputStream
            baos = new java.io.ByteArrayOutputStream();
            b64os = new OutputStream(baos, ENCODE | options);
            if ((options & GZIP) != 0) {
                // Gzip
                gzos = new java.util.zip.GZIPOutputStream(b64os);
                oos = new java.io.ObjectOutputStream(gzos);
            } else {
                // Not gzipped
                oos = new java.io.ObjectOutputStream(b64os);
            }
            oos.writeObject(serializableObject);
        } // end try
        catch (final IOException e) {
            // Catch it and then throw it immediately so that
            // the finally{} block is called for cleanup.
            throw e;
        } // end catch
        finally {
            try {
                oos.close();
            } catch (final Exception e) {
            }
            try {
                gzos.close();
            } catch (final Exception e) {
            }
            try {
                b64os.close();
            } catch (final Exception e) {
            }
            try {
                baos.close();
            } catch (final Exception e) {
            }
        } // end finally

        // Return value according to relevant encoding.
        try {
            return new String(baos.toByteArray(), PREFERRED_ENCODING);
        } // end try
        catch (final java.io.UnsupportedEncodingException uue) {
            // Fall back to some Java default
            return new String(baos.toByteArray());
        } // end catch

    } // end encode

    /**
     * Encode bytes.
     *
     * @param source the source
     * @return the string
     */
    public static String encodeBytes(byte[] source) {
        // Since we're not going to have the GZIP encoding turned on,
        // we're not going to have an java.io.IOException thrown, so
        // we should not force the user to have to catch it.
        String encoded = null;
        try {
            encoded = encodeBytes(source, 0, source.length, NO_OPTIONS);
        } catch (final IOException ex) {
            Logger.Error("Base64",ex.getMessage());
        }
        return encoded;
    }

    /**
     * Encode bytes.
     *
     * @param source the source
     * @param options the options
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String encodeBytes(byte[] source, int options) throws IOException {
        return encodeBytes(source, 0, source.length, options);
    } // end encodeBytes

    /**
     * Encode bytes.
     *
     * @param source the source
     * @param off the off
     * @param len the len
     * @param options the options
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static String encodeBytes(byte[] source, int off, int len, int options)
            throws IOException {
        final byte[] encoded = encodeBytesToBytes(source, off, len, options);

        // Return value according to relevant encoding.
        try {
            return new String(encoded, PREFERRED_ENCODING);
        } // end try
        catch (final java.io.UnsupportedEncodingException uue) {
            return new String(encoded);
        } // end catch

    } // end encodeBytes

    /**
     * Encode bytes to bytes.
     *
     * @param source the source
     * @param off the off
     * @param len the len
     * @param options the options
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static byte[] encodeBytesToBytes(byte[] source, int off, int len, int options)
            throws IOException {

        if (source == null)
            throw new NullPointerException("Cannot serialize a null array.");

        if (off < 0)
            throw new IllegalArgumentException("Cannot have negative offset: " + off);

        if (len < 0)
            throw new IllegalArgumentException("Cannot have length offset: " + len);

        if (off + len > source.length)
            throw new IllegalArgumentException(
                    String.format(Locale.US, "Cannot have offset of %d and length of %d with array of length %d", off, len,source.length)
            );

        // Compress?
        if ((options & GZIP) != 0) {
            java.io.ByteArrayOutputStream baos = null;
            java.util.zip.GZIPOutputStream gzos = null;
            OutputStream b64os = null;

            try {
                // GZip -> Base64 -> ByteArray
                baos = new java.io.ByteArrayOutputStream();
                b64os = new OutputStream(baos, ENCODE | options);
                gzos = new java.util.zip.GZIPOutputStream(b64os);

                gzos.write(source, off, len);
                gzos.close();
            } // end try
            catch (final IOException e) {
                // Catch it and then throw it immediately so that
                // the finally{} block is called for cleanup.
                throw e;
            } // end catch
            finally {
                try {
                    gzos.close();
                } catch (final Exception e) {
                }
                try {
                    b64os.close();
                } catch (final Exception e) {
                }
                try {
                    baos.close();
                } catch (final Exception e) {
                }
            } // end finally

            return baos.toByteArray();
        } // end if: compress

        // Else, don't compress. Better not to use streams at all then.
        else {
            final boolean breakLines = (options & DO_BREAK_LINES) != 0;

            // int len43 = len * 4 / 3;
            // byte[] outBuff = new byte[ ( len43 ) // Main 4:3
            // + ( (len % 3) > 0 ? 4 : 0 ) // Account for padding
            // + (breakLines ? ( len43 / MAX_LINE_LENGTH ) : 0) ]; // New lines
            // Try to determine more precisely how big the array needs to be.
            // If we get it right, we don't have to do an array copy, and
            // we save a bunch of memory.
            int encLen = (len / 3) * 4 + (len % 3 > 0 ? 4 : 0); // Bytes needed
            // for actual
            // encoding
            if (breakLines) {
                encLen += encLen / MAX_LINE_LENGTH; // Plus extra newline
                // characters
            }
            final byte[] outBuff = new byte[encLen];

            int d = 0;
            int e = 0;
            final int len2 = len - 2;
            int lineLength = 0;
            for (; d < len2; d += 3, e += 4) {
                encode3to4(source, d + off, 3, outBuff, e, options);

                lineLength += 4;
                if (breakLines && lineLength >= MAX_LINE_LENGTH) {
                    outBuff[e + 4] = NEW_LINE;
                    e++;
                    lineLength = 0;
                } // end if: end of line
            } // en dfor: each piece of array

            if (d < len) {
                encode3to4(source, d + off, len - d, outBuff, e, options);
                e += 4;
            } // end if: some padding needed

            // Only resize array if we didn't guess it right.
            if (e <= outBuff.length - 1) {
                // If breaking lines and the last byte falls right at
                // the line length (76 bytes per line), there will be
                // one extra byte, and the array will need to be resized.
                // Not too bad of an estimate on array size, I'd say.
                final byte[] finalOut = new byte[e];
                System.arraycopy(outBuff, 0, finalOut, 0, e);
                // System.err.println("Having to resize array from " +
                // outBuff.length + " to " + e );
                return finalOut;
            } else
                // System.err.println("No need to resize array.");
                return outBuff;

        } // end else: don't compress

    } // end encodeBytesToBytes

    /*  ******** D E C O D I N G M E T H O D S ******** */

    /**
     * Decode4to3.
     *
     * @param source the source
     * @param srcOffset the src offset
     * @param destination the destination
     * @param destOffset the dest offset
     * @param options the options
     * @return the int
     */
    private static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset,
                                  int options) {

        // Lots of error checking and exception throwing
        if (source == null)
            throw new NullPointerException("Source array was null.");
        if (destination == null)
            throw new NullPointerException("Destination array was null.");
        if (srcOffset < 0 || srcOffset + 3 >= source.length)
            throw new IllegalArgumentException(
                    String.format(Locale.US, "Source array with length %d cannot have offset of %d and still process four bytes.",source.length, srcOffset)
            );
        if (destOffset < 0 || destOffset + 2 >= destination.length)
            throw new IllegalArgumentException(
                    String.format(Locale.US, "Destination array with length %d cannot have offset of %d and still store three bytes.", destination.length, destOffset)
            );

        final byte[] DECODABET = getDecodabet(options);

        // Example: Dk==
        if (source[srcOffset + 2] == EQUALS_SIGN) {
            // Two ways to do the same thing. Don't know which way I like best.
            // int outBuff = ( ( DECODABET[ source[ srcOffset ] ] << 24 ) >>> 6
            // )
            // | ( ( DECODABET[ source[ srcOffset + 1] ] << 24 ) >>> 12 );
            final int outBuff = ((DECODABET[source[srcOffset]] & 0xFF) << 18)
                    | ((DECODABET[source[srcOffset + 1]] & 0xFF) << 12);

            destination[destOffset] = (byte)(outBuff >>> 16);
            return 1;
        }

        // Example: DkL=
        else if (source[srcOffset + 3] == EQUALS_SIGN) {
            // Two ways to do the same thing. Don't know which way I like best.
            // int outBuff = ( ( DECODABET[ source[ srcOffset ] ] << 24 ) >>> 6
            // )
            // | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
            // | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 );
            final int outBuff = ((DECODABET[source[srcOffset]] & 0xFF) << 18)
                    | ((DECODABET[source[srcOffset + 1]] & 0xFF) << 12)
                    | ((DECODABET[source[srcOffset + 2]] & 0xFF) << 6);

            destination[destOffset] = (byte)(outBuff >>> 16);
            destination[destOffset + 1] = (byte)(outBuff >>> 8);
            return 2;
        }

        // Example: DkLE
        else {
            // Two ways to do the same thing. Don't know which way I like best.
            // int outBuff = ( ( DECODABET[ source[ srcOffset ] ] << 24 ) >>> 6
            // )
            // | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
            // | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 )
            // | ( ( DECODABET[ source[ srcOffset + 3 ] ] << 24 ) >>> 24 );
            final int outBuff = ((DECODABET[source[srcOffset]] & 0xFF) << 18)
                    | ((DECODABET[source[srcOffset + 1]] & 0xFF) << 12)
                    | ((DECODABET[source[srcOffset + 2]] & 0xFF) << 6)
                    | ((DECODABET[source[srcOffset + 3]] & 0xFF));

            destination[destOffset] = (byte)(outBuff >> 16);
            destination[destOffset + 1] = (byte)(outBuff >> 8);
            destination[destOffset + 2] = (byte)(outBuff);

            return 3;
        }
    } // end decodeToBytes

    /**
     * Decode.
     *
     * @param source the source
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static byte[] decode(byte[] source) throws IOException {
        byte[] decoded = null;
        // try {
        decoded = decode(source, 0, source.length, Base64.NO_OPTIONS);
        // } catch( java.io.IOException ex ) {
        // assert false :
        // "IOExceptions only come from GZipping, which is turned off: " +
        // ex.getMessage();
        // }
        return decoded;
    }

    /**
     * Decode.
     *
     * @param source the source
     * @param off the off
     * @param len the len
     * @param options the options
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static byte[] decode(byte[] source, int off, int len, int options)
            throws IOException {

        // Lots of error checking and exception throwing
        if (source == null)
            throw new NullPointerException("Cannot decode null source array.");
        if (off < 0 || off + len > source.length)
            throw new IllegalArgumentException(
                    String.format(Locale.US, "Source array with length %d cannot have offset of %d and process %d bytes.", source.length, off, len)
            );

        if (len == 0)
            return new byte[0];
        else if (len < 4)
            throw new IllegalArgumentException(
                    "Base64-encoded string must have at least four characters, but length specified was "
                            + len);

        final byte[] DECODABET = getDecodabet(options);

        final int len34 = len * 3 / 4; // Estimate on array size
        final byte[] outBuff = new byte[len34]; // Upper limit on size of output
        int outBuffPosn = 0; // Keep track of where we're writing

        final byte[] b4 = new byte[4]; // Four byte buffer from source,
        // eliminating
        // white space
        int b4Posn = 0; // Keep track of four byte input buffer
        int i = 0; // Source array counter
        byte sbiDecode = 0; // Special value from DECODABET

        for (i = off; i < off + len; i++) { // Loop through source

            sbiDecode = DECODABET[source[i] & 0xFF];

            // White space, Equals sign, or legit Base64 character
            // Note the values such as -5 and -9 in the
            // DECODABETs at the top of the file.
            if (sbiDecode >= WHITE_SPACE_ENC) {
                if (sbiDecode >= EQUALS_SIGN_ENC) {
                    b4[b4Posn++] = source[i]; // Save non-whitespace
                    if (b4Posn > 3) { // Time to decode?
                        outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn, options);
                        b4Posn = 0;

                        // If that was the equals sign, break out of 'for' loop
                        if (source[i] == EQUALS_SIGN) {
                            break;
                        } // end if: equals sign
                    } // end if: quartet built
                } // end if: equals sign or better
            } // end if: white space, equals sign or better
            else
                // There's a bad input character in the Base64 stream.
                throw new IOException(
                        String.format(Locale.US, "Bad Base64 input character decimal %d in array position %d", (source[i]) & 0xFF, i));
        } // each input character

        final byte[] out = new byte[outBuffPosn];
        System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
        return out;
    } // end decode

    /**
     * Decode.
     *
     * @param s the s
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static byte[] decode(String s) throws IOException {
        return decode(s, NO_OPTIONS);
    }

    /**
     * Decode.
     *
     * @param s the s
     * @param options the options
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static byte[] decode(String s, int options) throws IOException {
        if (s == null)
            throw new NullPointerException("Input string was null.");

        byte[] bytes;
        try {
            bytes = s.getBytes(PREFERRED_ENCODING);
        } // end try
        catch (final java.io.UnsupportedEncodingException uee) {
            bytes = s.getBytes();
        } // end catch
        // </change>

        // Decode
        bytes = decode(bytes, 0, bytes.length, options);

        // Check to see if it's gzip-compressed
        // GZIP Magic Two-Byte Number: 0x8b1f (35615)
        final boolean dontGunzip = (options & DONT_GUNZIP) != 0;
        if ((bytes != null) && (bytes.length >= 4) && (!dontGunzip)) {

            final int head = (bytes[0] & 0xff) | ((bytes[1] << 8) & 0xff00);
            if (java.util.zip.GZIPInputStream.GZIP_MAGIC == head) {
                java.io.ByteArrayInputStream bais = null;
                java.util.zip.GZIPInputStream gzis = null;
                java.io.ByteArrayOutputStream baos = null;
                final byte[] buffer = new byte[2048];
                int length = 0;

                try {
                    baos = new java.io.ByteArrayOutputStream();
                    bais = new java.io.ByteArrayInputStream(bytes);
                    gzis = new java.util.zip.GZIPInputStream(bais);

                    while ((length = gzis.read(buffer)) >= 0) {
                        baos.write(buffer, 0, length);
                    } // end while: reading input

                    // No error? Get new bytes.
                    bytes = baos.toByteArray();

                } // end try
                catch (final IOException e) {
                    if(BuildConfig.DEBUG){
                        e.printStackTrace();
                    }
                    // Just return originally-decoded bytes
                } // end catch
                finally {
                    try {
                        baos.close();
                    } catch (final Exception e) {
                    }
                    try {
                        gzis.close();
                    } catch (final Exception e) {
                    }
                    try {
                        bais.close();
                    } catch (final Exception e) {
                    }
                } // end finally

            } // end if: gzipped
        } // end if: bytes.length >= 2

        return bytes;
    } // end decode

    /**
     * Decode to object.
     *
     * @param encodedObject the encoded object
     * @return the object
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public static Object decodeToObject(String encodedObject) throws IOException,
            ClassNotFoundException {
        return decodeToObject(encodedObject, NO_OPTIONS, null);
    }

    /**
     * Decode to object.
     *
     * @param encodedObject the encoded object
     * @param options the options
     * @param loader the loader
     * @return the object
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    private static Object decodeToObject(String encodedObject, int options, final ClassLoader loader)
            throws IOException, ClassNotFoundException {

        // Decode and gunzip if necessary
        final byte[] objBytes = decode(encodedObject, options);

        java.io.ByteArrayInputStream bais = null;
        java.io.ObjectInputStream ois = null;
        Object obj = null;

        try {
            bais = new java.io.ByteArrayInputStream(objBytes);

            // If no custom class loader is provided, use Java's builtin OIS.
            if (loader == null) {
                ois = new java.io.ObjectInputStream(bais);
            } // end if: no loader provided

            // Else make a customized object input stream that uses
            // the provided class loader.
            else {
                ois = new java.io.ObjectInputStream(bais) {
                    @SuppressWarnings("unchecked")
                    @Override
                    public Class<?> resolveClass(java.io.ObjectStreamClass streamClass)
                            throws IOException, ClassNotFoundException {
                        final Class c = Class.forName(streamClass.getName(), false, loader);
                        if (c == null)
                            return super.resolveClass(streamClass);
                        else
                            return c; // Class loader knows of this class.
                    } // end resolveClass
                }; // end ois
            } // end else: no custom class loader

            obj = ois.readObject();
        } // end try
        catch (final IOException e) {
            throw e; // Catch and throw in order to execute finally{}
        } // end catch
        catch (final ClassNotFoundException e) {
            throw e; // Catch and throw in order to execute finally{}
        } // end catch
        finally {
            try {
                bais.close();
            } catch (final Exception e) {
            }
            try {
                ois.close();
            } catch (final Exception e) {
            }
        } // end finally

        return obj;
    } // end decodeObject

    /**
     * Encode to file.
     *
     * @param dataToEncode the data to encode
     * @param filename the filename
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeToFile(byte[] dataToEncode, String filename)
            throws IOException {

        if (dataToEncode == null)
            throw new NullPointerException("Data to encode was null.");

        OutputStream bos = null;
        try {
            bos = new OutputStream(new java.io.FileOutputStream(filename), Base64.ENCODE);
            bos.write(dataToEncode);
        } // end try
        catch (final IOException e) {
            throw e; // Catch and throw to execute finally{} block
        } // end catch: java.io.IOException
        finally {
            try {
                bos.close();
            } catch (final Exception e) {
            }
        } // end finally

    } // end encodeToFile

    /**
     * Decode to file.
     *
     * @param dataToDecode the data to decode
     * @param filename the filename
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void decodeToFile(String dataToDecode, String filename)
            throws IOException {

        OutputStream bos = null;
        try {
            bos = new OutputStream(new java.io.FileOutputStream(filename), Base64.DECODE);
            bos.write(dataToDecode.getBytes(PREFERRED_ENCODING));
        } // end try
        catch (final IOException e) {
            throw e; // Catch and throw to execute finally{} block
        } // end catch: java.io.IOException
        finally {
            try {
                bos.close();
            } catch (final Exception e) {
            }
        } // end finally

    } // end decodeToFile

    /**
     * Decode from file.
     *
     * @param filename the filename
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static byte[] decodeFromFile(String filename) throws IOException {

        byte[] decodedData = null;
        InputStream bis = null;
        try {
            // Set up some useful variables
            final java.io.File file = new java.io.File(filename);
            byte[] buffer = null;
            int length = 0;
            int numBytes = 0;

            // Check for size of file
            if (file.length() > Integer.MAX_VALUE)
                throw new IOException("File is too big for this convenience method ("
                        + file.length() + " bytes).");
            buffer = new byte[(int)file.length()];

            // Open a stream
            bis = new InputStream(new java.io.BufferedInputStream(
                    new java.io.FileInputStream(file)), Base64.DECODE);

            // Read until done
            while ((numBytes = bis.read(buffer, length, 4096)) >= 0) {
                length += numBytes;
            } // end while

            // Save in a variable to return
            decodedData = new byte[length];
            System.arraycopy(buffer, 0, decodedData, 0, length);

        } // end try
        catch (final IOException e) {
            throw e; // Catch and release to execute finally{}
        } // end catch: java.io.IOException
        finally {
            try {
                bis.close();
            } catch (final Exception e) {
            }
        } // end finally

        return decodedData;
    } // end decodeFromFile

    /**
     * Encode from file.
     *
     * @param filename the filename
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static String encodeFromFile(String filename) throws IOException {

        String encodedData = null;
        InputStream bis = null;
        try {
            // Set up some useful variables
            final java.io.File file = new java.io.File(filename);
            final byte[] buffer = new byte[Math.max((int)(file.length() * 1.4 + 1), 40)]; // Need
            // max()
            // for
            // math
            // on
            // small
            // files
            // (v2.2.1);
            // Need
            // +1 for a few corner cases (v2.3.5)
            int length = 0;
            int numBytes = 0;

            // Open a stream
            bis = new InputStream(new java.io.BufferedInputStream(
                    new java.io.FileInputStream(file)), Base64.ENCODE);

            // Read until done
            while ((numBytes = bis.read(buffer, length, 4096)) >= 0) {
                length += numBytes;
            } // end while

            // Save in a variable to return
            encodedData = new String(buffer, 0, length, Base64.PREFERRED_ENCODING);

        } // end try
        catch (final IOException e) {
            throw e; // Catch and release to execute finally{}
        } // end catch: java.io.IOException
        finally {
            try {
                bis.close();
            } catch (final Exception e) {
            }
        } // end finally

        return encodedData;
    } // end encodeFromFile

    /**
     * Encode file to file.
     *
     * @param infile the infile
     * @param outfile the outfile
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeFileToFile(String infile, String outfile) throws IOException {

        final String encoded = Base64.encodeFromFile(infile);
        java.io.OutputStream out = null;
        try {
            out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(outfile));
            out.write(encoded.getBytes("US-ASCII")); // Strict, 7-bit output.
        } // end try
        catch (final IOException e) {
            throw e; // Catch and release to execute finally{}
        } // end catch
        finally {
            try {
                out.close();
            } catch (final Exception ex) {
            }
        } // end finally
    } // end encodeFileToFile

    /**
     * Decode file to file.
     *
     * @param infile the infile
     * @param outfile the outfile
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void decodeFileToFile(String infile, String outfile) throws IOException {

        final byte[] decoded = Base64.decodeFromFile(infile);
        java.io.OutputStream out = null;
        try {
            out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(outfile));
            out.write(decoded);
        } // end try
        catch (final IOException e) {
            throw e; // Catch and release to execute finally{}
        } // end catch
        finally {
            try {
                out.close();
            } catch (final Exception ex) {
            }
        } // end finally
    } // end decodeFileToFile

    /*  ******** I N N E R C L A S S I N P U T S T R E A M ******** */

    /**
     * The Class InputStream.
     */
    public static class InputStream extends java.io.FilterInputStream {

        /** The encode. */
        private final boolean encode; // Encoding or decoding

        /** The position. */
        private int position; // Current position in the buffer

        /** The buffer. */
        private final byte[] buffer; // Small buffer holding converted data

        /** The buffer length. */
        private final int bufferLength; // Length of buffer (3 or 4)

        /** The num sig bytes. */
        private int numSigBytes; // Number of meaningful bytes in the buffer

        /** The line length. */
        private int lineLength;

        /** The break lines. */
        private final boolean breakLines; // Break lines at less than 80
        // characters

        /** The options. */
        private final int options; // Record options used to create the stream.

        /** The decodabet. */
        private final byte[] decodabet; // Local copies to avoid extra method
        // calls

        /**
         * Instantiates a new input stream.
         *
         * @param in the in
         */
        public InputStream(java.io.InputStream in) {
            this(in, DECODE);
        } // end constructor

        /**
         * Instantiates a new input stream.
         *
         * @param in the in
         * @param options the options
         */
        public InputStream(java.io.InputStream in, int options) {

            super(in);
            this.options = options; // Record for later
            this.breakLines = (options & DO_BREAK_LINES) > 0;
            this.encode = (options & ENCODE) > 0;
            this.bufferLength = this.encode ? 4 : 3;
            this.buffer = new byte[this.bufferLength];
            this.position = -1;
            this.lineLength = 0;
            this.decodabet = getDecodabet(options);
        } // end constructor

        /* (non-Javadoc)
         * @see java.io.FilterInputStream#read()
         */
        @Override
        public int read() throws IOException {

            // Do we need to get data?
            if (this.position < 0) {
                if (this.encode) {
                    final byte[] b3 = new byte[3];
                    int numBinaryBytes = 0;
                    for (int i = 0; i < 3; i++) {
                        final int b = this.in.read();

                        // If end of stream, b is -1.
                        if (b >= 0) {
                            b3[i] = (byte)b;
                            numBinaryBytes++;
                        } else {
                            break; // out of for loop
                        } // end else: end of stream

                    } // end for: each needed input byte

                    if (numBinaryBytes > 0) {
                        encode3to4(b3, 0, numBinaryBytes, this.buffer, 0, this.options);
                        this.position = 0;
                        this.numSigBytes = 4;
                    } // end if: got data
                    else
                        return -1; // Must be end of stream
                } // end if: encoding

                // Else decoding
                else {
                    final byte[] b4 = new byte[4];
                    int i = 0;
                    for (i = 0; i < 4; i++) {
                        // Read four "meaningful" bytes:
                        int b = 0;
                        do {
                            b = this.in.read();
                        } while (b >= 0 && this.decodabet[b & 0x7f] <= WHITE_SPACE_ENC);

                        if (b < 0) {
                            break; // Reads a -1 if end of stream
                        } // end if: end of stream

                        b4[i] = (byte)b;
                    } // end for: each needed input byte

                    if (i == 4) {
                        this.numSigBytes = decode4to3(b4, 0, this.buffer, 0, this.options);
                        this.position = 0;
                    } // end if: got four characters
                    else if (i == 0)
                        return -1;
                    else
                        // Must have broken out from above.
                        throw new IOException("Improperly padded Base64 input.");

                } // end else: decode
            } // end else: get data

            // Got data?
            if (this.position >= 0) {
                // End of relevant data?
                if ( /* !encode && */this.position >= this.numSigBytes)
                    return -1;

                if (this.encode && this.breakLines && this.lineLength >= MAX_LINE_LENGTH) {
                    this.lineLength = 0;
                    return '\n';
                } // end if
                else {
                    this.lineLength++; // This isn't important when decoding
                    // but throwing an extra "if" seems
                    // just as wasteful.

                    final int b = this.buffer[this.position++];

                    if (this.position >= this.bufferLength) {
                        this.position = -1;
                    } // end if: end

                    return b & 0xFF; // This is how you "cast" a byte that's
                    // intended to be unsigned.
                } // end else
            } // end if: position >= 0
            else
                throw new IOException("Error in Base64 code reading stream.");
        } // end read

        /* (non-Javadoc)
         * @see java.io.FilterInputStream#read(byte[], int, int)
         */
        @Override
        public int read(byte[] dest, int off, int len) throws IOException {
            int i;
            int b;
            for (i = 0; i < len; i++) {
                b = this.read();

                if (b >= 0) {
                    dest[off + i] = (byte)b;
                } else if (i == 0)
                    return -1;
                else {
                    break; // Out of 'for' loop
                } // Out of 'for' loop
            } // end for: each byte read
            return i;
        } // end read

    } // end inner class InputStream

    /*  ******** I N N E R C L A S S O U T P U T S T R E A M ******** */

    /**
     * The Class OutputStream.
     */
    public static class OutputStream extends java.io.FilterOutputStream {

        /** The encode. */
        private final boolean encode;

        /** The position. */
        private int position;

        /** The buffer. */
        private byte[] buffer;

        /** The buffer length. */
        private final int bufferLength;

        /** The line length. */
        private int lineLength;

        /** The break lines. */
        private final boolean breakLines;

        /** The b4. */
        private final byte[] b4; // Scratch used in a few places

        /** The suspend encoding. */
        private boolean suspendEncoding;

        /** The options. */
        private final int options; // Record for later

        /** The decodabet. */
        private final byte[] decodabet; // Local copies to avoid extra method
        // calls

        /**
         * Instantiates a new output stream.
         *
         * @param out the out
         */
        public OutputStream(java.io.OutputStream out) {
            this(out, ENCODE);
        } // end constructor

        /**
         * Instantiates a new output stream.
         *
         * @param out the out
         * @param options the options
         */
        public OutputStream(java.io.OutputStream out, int options) {
            super(out);
            this.breakLines = (options & DO_BREAK_LINES) != 0;
            this.encode = (options & ENCODE) != 0;
            this.bufferLength = this.encode ? 3 : 4;
            this.buffer = new byte[this.bufferLength];
            this.position = 0;
            this.lineLength = 0;
            this.suspendEncoding = false;
            this.b4 = new byte[4];
            this.options = options;
            this.decodabet = getDecodabet(options);
        } // end constructor

        /* (non-Javadoc)
         * @see java.io.FilterOutputStream#write(int)
         */
        @Override
        public void write(int theByte) throws IOException {
            // Encoding suspended?
            if (this.suspendEncoding) {
                this.out.write(theByte);
                return;
            } // end if: supsended

            // Encode?
            if (this.encode) {
                this.buffer[this.position++] = (byte)theByte;
                if (this.position >= this.bufferLength) { // Enough to encode.

                    this.out
                            .write(encode3to4(this.b4, this.buffer, this.bufferLength, this.options));

                    this.lineLength += 4;
                    if (this.breakLines && this.lineLength >= MAX_LINE_LENGTH) {
                        this.out.write(NEW_LINE);
                        this.lineLength = 0;
                    } // end if: end of line

                    this.position = 0;
                } // end if: enough to output
            } // end if: encoding

            // Else, Decoding
            else {
                // Meaningful Base64 character?
                if (this.decodabet[theByte & 0x7f] > WHITE_SPACE_ENC) {
                    this.buffer[this.position++] = (byte)theByte;
                    if (this.position >= this.bufferLength) { // Enough to
                        // output.

                        final int len = Base64.decode4to3(this.buffer, 0, this.b4, 0, this.options);
                        this.out.write(this.b4, 0, len);
                        this.position = 0;
                    } // end if: enough to output
                } // end if: meaningful base64 character
                else if (this.decodabet[theByte & 0x7f] != WHITE_SPACE_ENC)
                    throw new IOException("Invalid character in Base64 data.");
            } // end else: decoding
        } // end write

        /* (non-Javadoc)
         * @see java.io.FilterOutputStream#write(byte[], int, int)
         */
        @Override
        public void write(byte[] theBytes, int off, int len) throws IOException {
            // Encoding suspended?
            if (this.suspendEncoding) {
                this.out.write(theBytes, off, len);
                return;
            } // end if: supsended

            for (int i = 0; i < len; i++) {
                this.write(theBytes[off + i]);
            } // end for: each byte written

        } // end write

        /**
         * Flush base64.
         *
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public void flushBase64() throws IOException {
            if (this.position > 0) {
                if (this.encode) {
                    this.out.write(encode3to4(this.b4, this.buffer, this.position, this.options));
                    this.position = 0;
                } // end if: encoding
                else
                    throw new IOException("Base64 input not properly padded.");
            } // end if: buffer partially full

        } // end flush

        /* (non-Javadoc)
         * @see java.io.FilterOutputStream#close()
         */
        @Override
        public void close() throws IOException {
            // 1. Ensure that pending characters are written
            this.flushBase64();

            // 2. Actually close the stream
            // Base class both flushes and closes.
            super.close();

            this.buffer = null;
            this.out = null;
        } // end close

        /**
         * Suspend encoding.
         *
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public void suspendEncoding() throws IOException {
            this.flushBase64();
            this.suspendEncoding = true;
        } // end suspendEncoding

        /**
         * Resume encoding.
         */
        public void resumeEncoding() {
            this.suspendEncoding = false;
        } // end resumeEncoding

    } // end inner class OutputStream

} // end class Base64
