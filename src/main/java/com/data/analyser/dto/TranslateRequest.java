package com.data.analyser.dto;

public class TranslateRequest {

    private String input_lang;
    private String output_lang;
    private String text;

    public TranslateRequest(String input_lang, String output_lang, String text) {
        this.input_lang = input_lang;
        this.output_lang = output_lang;
        this.text = text;
    }

    public static TranslateRequest createEnToFrTranslateRequest(String text){
        return new TranslateRequest("en","fr",text);
    }

    public String getInput_lang() {
        return input_lang;
    }

    public String getOutput_lang() {
        return output_lang;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
