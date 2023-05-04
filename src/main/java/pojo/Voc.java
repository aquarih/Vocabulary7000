package pojo;

public class Voc {
    private String word;
    private String speech;
    private Integer level;

    public Voc() {}

    public Voc(String word, String speech, Integer level) {
        this.word = word;
        this.speech = speech;
        this.level = level;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getSpeech() {
        return speech;
    }

    public void setSpeech(String speech) {
        this.speech = speech;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }
}
