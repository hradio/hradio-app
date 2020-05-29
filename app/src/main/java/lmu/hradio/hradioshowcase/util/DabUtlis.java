package lmu.hradio.hradioshowcase.util;

import android.os.Bundle;

import org.omri.radioservice.RadioService;
import org.omri.radioservice.RadioServiceDab;
import org.omri.radioservice.metadata.TextualDabDynamicLabel;
import org.omri.radioservice.metadata.TextualDabDynamicLabelPlusItem;

import lmu.hradio.hradioshowcase.model.view.TextData;
import lmu.hradio.hradioshowcase.model.view.TextData.TextType;

public final class DabUtlis {

    public static TextData parseDLPlus(TextualDabDynamicLabel label) {
        String content = "", title = "";
        TextType type = null;
        if (label.hasTags()) {
            for (TextualDabDynamicLabelPlusItem item : label.getDlPlusItems()) {
                switch (item.getDynamicLabelPlusContentType()) {
                    case ITEM_ARTIST:
                        content = item.getDlPlusContentText();
                        type = TextType.Track;
                        break;

                    case ITEM_TITLE:
                        title = item.getDlPlusContentText();
                        /*
                        if (title.contains("-")) {
                            title = title.split("-", 2)[0].trim();
                        }
                        title = title.replaceAll("\\*[^*]*\\*", "").replaceAll("[^a-zA-Z0-9\\s]", "");
                        */

                        break;
                    case ITEM_ALBUM:
                        break;
                    case ITEM_GENRE:
                        break;
                    case SMS_STUDIO:
                        type = TextType.SMS;

                        content = item.getDlPlusContentText();
                        break;
                    case CHAT_CENTER:
                        type = TextType.WEB;

                        content = item.getDlPlusContentText();
                        break;
                    case EMAIL_OTHER:
                        type = TextType.EMAIL;

                        content = item.getDlPlusContentText();
                        break;
                    case INFO_CINEMA:
                        break;
                    case INFO_HEALTH:
                        break;
                    case PHONE_OTHER:
                        type = TextType.Phone;

                        content = item.getDlPlusContentText();
                        break;
                    case VOTE_CENTRE:
                        type = TextType.WEB;

                        content = item.getDlPlusContentText();
                        break;
                    case EMAIL_STUDIO:
                        type = TextType.EMAIL;

                        content = item.getDlPlusContentText();
                        break;
                    case INFO_Lottery:
                        type = TextType.News;
                        break;
                    case INFO_TRAFFIC:
                        type = TextType.News;
                        break;
                    case INFO_WEATHER:
                        type = TextType.News;
                        break;
                    case ITEM_COMMENT:
                        break;
                    case PHONE_STUDIO:
                        type = TextType.Phone;

                        content = item.getDlPlusContentText();
                        break;
                    case EMAIL_HOTLINE:
                        type = TextType.EMAIL;

                        content = item.getDlPlusContentText();
                        break;
                    case ITEM_COMPOSER:
                        break;
                    case ITEM_MOVEMENT:
                        break;
                    case PHONE_HOTLINE:
                        type = TextType.Phone;
                        content = item.getDlPlusContentText();
                        break;
                    case PROGRAMME_NOW:
                        break;
                    case VOTE_QUESTION:
                        title = item.getDlPlusContentText();
                        break;
                    case INFO_DATE_TIME:
                        break;
                    case INFO_HOROSCOPE:
                        break;
                    case ITEM_CONDUCTOR:
                        break;
                    case PROGRAMME_HOST:
                        content = item.getDlPlusContentText();
                        break;
                    case PROGRAMME_NEXT:
                        break;
                    case PROGRAMME_PART:
                        break;
                    case INFO_NEWS_LOCAL:
                        type = TextType.News;
                        break;
                    case PRIVATE_CLASS_1:
                        break;
                    case PRIVATE_CLASS_2:
                        break;
                    case PRIVATE_CLASS_3:
                        break;
                    case DESCRIPTOR_PLACE:
                        break;
                    case INFO_STOCKMARKET:
                        break;
                    case ITEM_COMPOSITION:
                        break;
                    case ITEM_TRACKNUMBER:
                        break;
                    case STATIONNAME_LONG:
                        break;
                    case STATIONNAME_SHORT:
                        break;
                    case INFO_ADVERTISEMENT:
                        break;
                    case DUMMY:
                        break;
                    case PROGRAMME_HOMEPAGE:
                        type = TextType.WEB;
                        content = item.getDlPlusContentText();
                        break;
                    case DESCRIPTOR_GET_DATA:
                        break;
                    case DESCRIPTOR_PURCHASE:
                        break;
                    case PROGRAMME_FREQUENCY:
                        break;
                    case INFO_DAILY_DIVERSION:
                        break;
                    case PROGRAMME_SUBCHANNEL:
                        break;
                    case DESCRIPTOR_IDENTIFIER:
                        break;
                    case DESCRIPTOR_APPOINTMENT:
                        break;
                    case PROGRAMME_EDITORIAL_STAFF:
                        break;
                    case CHAT:
                        break;
                    case RFU_1:
                        break;
                    case RFU_2:
                        break;
                    case INFO_TV:
                        type = TextType.News;
                        break;
                    case INFO_URL:
                        type = TextType.WEB;
                        content = item.getDlPlusContentText();
                        break;
                    case INFO_NEWS:
                        type = TextType.News;
                        break;
                    case ITEM_BAND:
                        break;
                    case MMS_OTHER:
                        break;
                    case SMS_OTHER:
                        type = TextType.SMS;
                        content = item.getDlPlusContentText();
                        break;
                    case INFO_ALARM:
                        type = TextType.News;
                        break;
                    case INFO_EVENT:
                        type = TextType.News;
                        break;
                    case INFO_OTHER:
                        type = TextType.News;
                        break;
                    case INFO_SCENE:
                        type = TextType.News;
                        break;
                    case INFO_SPORT:
                        type = TextType.News;
                        break;
                }
            }
        }
        if(!content.isEmpty() && title.isEmpty())
            title = label.getText();
        if(!title.isEmpty() && !content.isEmpty() && title.endsWith(content) && !title.equals(content)){
            title = title.substring(0, title.length() - content.length());
        }

        return new TextData(type, label.getText(), content, title);
    }

    public static boolean filter(RadioService radioService, Bundle bundle) {
        String name = bundle.getString(Keys.NAME);
        if(name != null && !radioService.getServiceLabel().contains(name))
            return true;

        String providerNameService = ((RadioServiceDab) radioService).getEnsembleLabel();
        String providerName = bundle.getString(Keys.PROVIDER);
        if(providerName != null && !providerName.equals(providerNameService))
            return true;

        return false;
    }

    private interface Keys {
        String PROVIDER = "providerName";
        String NAME = "name";
    }
}
