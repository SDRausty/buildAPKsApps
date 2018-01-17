package ch.fixme.cowsay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class Cow {
    public String style;
    public String message = "Moo";
    public int think = 0;
    private String rawCow = "";

    public static final int FACE_DEFAULT = 0;
    public static final int FACE_BORG = 1;
    public static final int FACE_DEAD = 2;
    public static final int FACE_GREEDY = 3;
    public static final int FACE_PARANOID = 4;
    public static final int FACE_STONED = 5;
    public static final int FACE_TIRED = 6;
    public static final int FACE_WIRED = 7;
    public static final int FACE_YOUNG = 8;

    // Borders: up-left, up-right, down-left, down-right, left, right
    private static final char[] border1 = new char[] { '(', ')', '(', ')', '(', ')' };
    private static final char[] border2 = new char[] { '/', '\\', '\\', '/', '|', '|' };
    private static final char[] border3 = new char[] { '<', '>' };

    private static final String[] token_src = new String[] { "$eyes", "${eyes}", "$tongue",
            "${tongue}", "$thoughts", "${thoughts}", "\\@", "\\\\" };
    private String[] token_dst;

    private final int WRAPLEN = 30; // TODO: This should not be fixed here?
    private static final String TAG = "Cow";
    public static final String LF = "\n";

    private final Context context;
    private final AssetManager mngr;
    public int face = FACE_DEFAULT;

    public Cow(Context context) {
        this.context = context;
        this.mngr = context.getAssets();
        getCowFile();
        constructFace(FACE_DEFAULT);
    }

    public String getFinalCow() {
        // TODO: Use a StringBuffer or TextUtils.replace() ?
        String newCow = new String(rawCow);
        for (int i = 0; i < token_src.length; i++) {
            newCow = newCow.replace(token_src[i], token_dst[i]);
        }
        return getBalloon() + newCow;
    }

    public String[] getCowTypes() {
        ArrayList<String> res = new ArrayList<String>();
        try {
            String[] cows = context.getAssets().list("cows");
            for (int i = 0; i < cows.length; i++) {
                String string = cows[i];
                res.add(string.substring(0, string.length() - 4));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (String[]) res.toArray(new String[res.size()]);
    }

    private String getBalloon() {
        int msglen = message.length();
        int maxlen = (msglen > WRAPLEN) ? WRAPLEN : msglen;
        // Replace LF by spaces
        if (message.contains(LF)) {
            // int sublen;
            int rfpos;
            StringBuffer sb = new StringBuffer();
            String part = "";
            for (int i = 0; i < msglen;) {
                // Get the next line
                if (i + WRAPLEN < msglen) {
                    part = message.substring(i, i + WRAPLEN);
                } else {
                    part = message.substring(i, msglen);
                }
                if (part.contains(LF)) {
                    // Remove the first LF and replace by spaces
                    rfpos = part.indexOf(LF);
                    part = part.substring(0, rfpos);
                    sb.append(part).append(
                            new String(new char[WRAPLEN - part.length()]).replace("\0", " "));
                    // Restart after the line break
                    i += rfpos + 1;
                } else {
                    sb.append(part);
                    i += part.length();
                }
            }
            message = sb.toString();
            msglen = message.length();
            maxlen = WRAPLEN;
        }
        // Balloon borders
        final char[] border;
        if (think == 1) {
            border = border1;
        } else if (msglen > WRAPLEN) {
            border = border2;
        } else {
            border = border3;
        }
        // Draw balloon content
        StringBuffer balloon = new StringBuffer();
        // TODO: append(char[] chars, int start, int length)
        balloon.append(" ").append(new String(new char[maxlen + 2]).replace("\0", "_")).append(LF);
        if (msglen > WRAPLEN) {
            for (int i = 0; i < msglen; i += WRAPLEN) {
                // First line
                if (i < WRAPLEN) {
                    balloon.append(border[0]).append(" ").append(message.substring(0, WRAPLEN))
                            .append(" ").append(border[1]).append(LF);
                } else {
                    // Last line
                    int sublen = message.substring(i, msglen - 1).length();
                    if (sublen < WRAPLEN) {
                        int padlen = WRAPLEN - sublen;
                        String padding = new String(new char[padlen]).replace("\0", " ");
                        balloon.append(border[2]).append(" ").append(message.substring(i, msglen))
                                .append(padding).append(border[3]).append(LF);
                        // Middle line
                    } else {
                        balloon.append(border[4]).append(" ")
                                .append(message.substring(i, i + WRAPLEN)).append(" ")
                                .append(border[5]).append(LF);
                    }
                }
            }
        } else {
            balloon.append(border[0]).append(" ").append(message).append(" ").append(border[1])
                    .append(LF);
        }

        balloon.append(" ").append(new String(new char[maxlen + 2]).replace("\0", "-")).append(LF);
        return balloon.toString();
    }

    public void getCowFile() {
        if (style == null) {
            Log.e(TAG, "FIXME: cow style is null"); // FIXME
            return;
        }
        try {
            InputStream is = mngr.open("cows/" + style + ".cow");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            // Jump to cow start
            while (true) {
                line = br.readLine();
                Log.d(TAG, "Line: " + line);
                if (line == null) {
                    rawCow = context.getString(R.string.error_parse);
                }
                if (line.contains("$the_cow =")) {
                    break;
                }
            }
            Log.d(TAG, "Got the cow!");
            while ((line = br.readLine()) != null) {
                Log.d(TAG, "Line: " + line);
                if ((line.contains("EOC") || line.contains("EOF"))) {
                    Log.d(TAG, "End of cow found");
                    break;
                }
                sb.append(line).append(LF);
            }
            rawCow = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            rawCow = context.getString(R.string.error_crash);
        }
    }

    public void constructFace(int face) {
        this.face = face;
        final String thoughts;
        if (think == 1) {
            thoughts = "o";
        } else {
            thoughts = "\\";
        }
        String eyes = "oo";
        String tongue = "  ";
        switch (face) {
            case FACE_BORG:
                eyes = "==";
                tongue = "  ";
                break;
            case FACE_DEAD:
                eyes = "xx";
                tongue = "U ";
                break;
            case FACE_GREEDY:
                eyes = "$$";
                tongue = "  ";
                break;
            case FACE_PARANOID:
                eyes = "@@";
                tongue = "  ";
                break;
            case FACE_STONED:
                eyes = "**";
                tongue = "U ";
                break;
            case FACE_WIRED:
                eyes = "00";
                tongue = "  ";
                break;
            case FACE_YOUNG:
            case FACE_TIRED:
                eyes = "..";
                tongue = "  ";
                break;
        }
        token_dst = new String[] { eyes, eyes, tongue, tongue, thoughts, thoughts, "@", "\\" };
    }
}
