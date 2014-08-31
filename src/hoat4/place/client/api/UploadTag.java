/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hoat4.place.client.api;

/**
 *
 * @author hontvaria
 */
/*public class UploadTag {

    public static final UploadTag NON_SPECIFIED = create("unspecified");
    public static final UploadTag JAVA_CODE = create("javacode");
    public static final UploadTag OTHER_CODE = create("misccode");
    public static final UploadTag HOANET_TEST = create("uploadtest");
    public static final UploadTag TXT_HU = create("hutext");
    public static final UploadTag TXT_EN = create("entext");

    public static UploadTag get(String string) {
        switch (string.toLowerCase()) {
            case "java":
            case "javacode":
            case "java_lang":
            case "lang_java":
            case "app_java":
            case "java_app":
            case "prog_java":
            case "appjava":
            case "beanshell":
            case "bsh":
            case "codejava":
            case "java_code":
            case "j":
                return JAVA_CODE;
            case "misc":
            case "misccode":
            case "misc_code":
            case "other_code":
            case "othercode":
            case "code_misc":
            case "code_other_lang":
            case "code_other":
            case "code":
            case "app":
            case "js":
            case "html":
                return OTHER_CODE;
            case "test":
            case "hoatest":
            case "hoanet_test":
            case "place_test":
            case "demo":
            case "placedemo":
            case "place_demo":
                return HOANET_TEST;
            case "hu":
            case "hun":
            case "magyar":
            case "hungarian":
            case "txt_hu":
            case "txt_hun":
            case "txt_hungarian":
            case "hu_txt":
            case "hun_txt":
            case "text_hu":
            case "text_hun":
            case "text_hungarian":
            case "hu_text":
            case "hun_text":
            case "hungarian_text":
            case "hutxt":
            case "huntxt":
            case "txthu":
            case "texthu":
                return TXT_HU;
            case "en":
            case "eng":
            case "english":
            case "angol":
            case "txt_en":
            case "txt_eng":
            case "txt_english":
            case "en_txt":
            case "eng_txt":
            case "text_en":
            case "text_eng":
            case "text_english":
            case "en_text":
            case "eng_text":
            case "english_text":
            case "entxt":
            case "entext":
            case "txten":
            case "texten":
            case "entxtg":
            case "entextg":
            case "txteng":
            case "texteng":
                return TXT_EN;
        }
        return NON_SPECIFIED;
    }
    String val;

    private static UploadTag create(String val) {
        UploadTag uploadTag = new UploadTag();
        uploadTag.val = val;
        return uploadTag;
    }
    private static final ConcurrentMap<String, UploadTag> cache = new ConcurrentHashMap<String, UploadTag>();

    public static UploadTag custom(String tag) {
        UploadTag res = cache.get(tag);
        if (res == null) {
            res = new UploadTag();
            res.val = tag;
            cache.put(tag, res);
        }
        return res;
    }
}*/
