package lmu.hradio.hradioshowcase.model.view;

import android.os.Parcel;
import android.os.Parcelable;

import org.omri.radioservice.metadata.Textual;
import org.omri.radioservice.metadata.TextualDabDynamicLabel;
import org.omri.radioservice.metadata.TextualType;

import eu.hradio.substitutionapi.Substitution;
import lmu.hradio.hradioshowcase.util.DabUtlis;

public class TextData implements Parcelable {

    private String text;
    private String content;
    private String title;

    private TextType textType;

    public TextData(String text) {
        this(TextType.None, text, "", "");
    }

    public TextData(TextType textType, String text, String content, String title) {
        this.text = text;
        // TODO: fix not existing title/content in PlatformSearch
        if(content!=null)  this.content = content;
        else this.content = "";
        if(title!=null)  this.title = title;
        else this.title = "";
        this.textType = textType;
        if(this.title.isEmpty() && !this.content.isEmpty())
            this.title = text;
    }

    public TextData(String song, String content) {
        this(TextType.None,song + " - " + content, content, song);
    }

    protected TextData(Parcel in) {
        text = in.readString();
        content = in.readString();
        title = in.readString();
        textType = TextType.values()[in.readInt()];
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(content);
        dest.writeString(title);
        dest.writeInt(textType.ordinal());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TextData> CREATOR = new Creator<TextData>() {
        @Override
        public TextData createFromParcel(Parcel in) {
            return new TextData(in);
        }

        @Override
        public TextData[] newArray(int size) {
            return new TextData[size];
        }
    };

    public boolean hasLabels(){
        return !content.isEmpty() && !title.isEmpty();
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object other){
        if (other instanceof TextData){
            TextData otherData = (TextData) other;
            return otherData.text.equals(text) && otherData.content.equals(content) && otherData.title.equals(title);
        }
        return false;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TextType getTextType() {
        return textType;
    }

    public void setTextType(TextType textType) {
        this.textType = textType;
    }

    public static TextData fromTextual(Textual textual){
        if (textual.getType() == TextualType.METADATA_TEXTUAL_TYPE_DAB_DLS) {
            TextualDabDynamicLabel textualDabDynamicLabel = (TextualDabDynamicLabel) textual;
            return DabUtlis.parseDLPlus(textualDabDynamicLabel);
        }
            return new TextData(textual.getText());
        }

    public static TextData fromSubstitution(Substitution substitution) {
        return new TextData(substitution.getArtist(), substitution.getTitle());
    }

    public enum TextType {
        Track, News, Phone, SMS, EMAIL, WEB , None
    }

}
