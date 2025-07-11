package com.dhammadownload.dhammadownloadandroid.common;

/**
 * Created by zawlinaung on 9/16/16.
 */
public class Constants {

    public static final String mainURL  ="https://dhammadownload.com";//"http://192.168.43.1:8080";

    public static final String mainURLProcol  = "https:/";

    public static final String mainFolder  = "dhammadownload.com";
    public static final String INTENT_URL="INTENT_URL";

    public static final String dhammadownloadTitle_EN  = "DHAMMADOWNLOAD";

    public static final String dhammadownloadTitile_MM = "ဓမ္မဒေါင်းလုပ်";//"ဓမ\u107Cေဒ\u102Bင္းလုပ္";

    public static final String tabActiveFontColor  = "#2980b9";
    public static final String tabDeactiveFontColor  = "#666666";
    public static final String tabBackgroundColor  = "#ecf0f1";

    public static final String tabLabelOnline  = "အွန်လိုင်း";
    public static final String tabLabelMP3  = "အသံ";
    public static final String tabLabelMP4  = "ဗီဒီယို";
    public static final String tabLabelEBOOK  = "တရားစာအုပ်များ";//"တရားစာအုပ္မ\u103Aား";
    public static final String tabLabelPLAYER = "Player";
    public static final String tabLabelSetting  = "Setting";
    public static final String dhammadownloadTitile = "ဓမ္မဒေါင်းလုပ်";//"ဓမ\u107Cေဒ\u102Bင္းလုပ္";

    public static final String fileDownloadedMessage = "ဤတရားေတာ္မ\u103Dာ ဖုန္းထဲတ\u103Cင္ သိမ္းဆည္းထား \u103Bပီးပ\u102B\u103Bပီ။";
    public static final String fileToDownloadedMessage = "ဤတရားေတာ္ကို သိမ္းဆည္းလိုပ\u102Bက DOWNLOAD ခလုပ္ ကို \u108F\u103Dိပ္ပ\u102B။";
    public static final String fileDownloadedMessageForSDCard = "ဤတရားေတာ္မွာ External SD Card ထဲတြင္ သိမ္းဆည္းထား ျပီးပါျပီ။";
    public static final String fileToDownloadedMessageForSDCard = "ဤတရားေတာ္ကို သိမ္းဆည္းလိုပါက DOWNLOAD ခလုပ္ ကို ႏွိပ္ပါ။ External SD Card ေပၚတြင္ သိမ္းဆည္းမည္။";

    public static final String standardFont  = "fonts/Mk10M.ttf";

    public static final int tabLabelFontSize  = 10;
    public static final int BodyFontSize1  = 15;

    public static final String[] supportedDownloadFiles = {"MP3","MP4","PDF"};
    public static final String[] supportedAudioFiles = {"MP3"};
    public static final String[] supportedVideoFiles = {"MP4",};
    public static final String[] supportedEbookFiles = {"PDF"};

    public static final String txtAudioFileDesc  = "MP3 တရားတော်";
    public static final String txtVideoFileDesc  = "MP4 တရားေတာ္";
    public static final String txtEbookFileDesc  = "တရားစာအုပ္";

    public static final String authorImageExt = ".gif"; //Author profile photo file extenstion
    public static final String authorMainConfigFile = "main.conf"; //file where all author info are stored name etc.
    public static final String authorMediaConfig = "mediainfo.conf"; //file where mp3 files and myanmar names mapping are stored.

    public static final String locaMP3ListHeader = "သိမ္းဆည္း MP3  တရားမ\u103Aား";
    public static final String locaMP4ListHeader = "သိမ္းဆည္း MP4  တရားမ\u103Aား";
    public static final String locaMEBOOKListHeader = "သိမ္းဆည္း တရားစာအုပ္မ\u103Aား";

    public static final String localMP3ListEmptyMsg = "ဖုန်းထဲတွင် သိမ်းဆည်းထားသော MP3 တရား မရှိသေးပါ။ 'အွန်လိုင်း' ေနရာတြင္ သြားေရာက္၍ တရားေတာ္မ်ားကို အရင္ဦးစြာ ရယူပါရန္..";
    public static final String localMP4ListEmptyMsg = "ဖုန်းထဲတွင် သိမ်းဆည်းထားသော MP4 တရားဗီဒီယို မရှိသေးပါ။ 'အွန်လိုင်း' ေနရာတြင္ သြားေရာက္၍ တရားေတာ္မ်ားကို အရင္ဦးစြာ ရယူပါရန္..";
    public static final String localEBOOKListEmptyMsg = "ဖုန်းထဲတွင် သိမ်းဆည်းထားသော pdf တရားစာအုပ္ မရှိသေးပါ။ 'အွန်လိုင်း' ေနရာတြင္ သြားေရာက္၍ တရားေတာ္မ်ားကို အရင္ဦးစြာ ရယူပါရန္..";
    public static final String localTextToBold = "'အွန်လိုင်း'";

    public static final String DownloadErrorMsg="Error တက်နေပါသဖြင့် Download မရနိုင်ပါ။";

    public static final String SDCardNotWritableMsg="No External SD Card is available to store downloaded Dhamma. Storage Setting will be changed to Internal.";

    public static final String MainStoragePermissionRequestMsg="တရားတော်များကို သိမ်းဆည်းရန် DhammaDownload အား Storage Permission ကို ခွင့်ပြုပေးရန် လိုအပ်ပါသည်။";
    public static final String StoragePermissionRequestMsg="ဤတရားေတာ္ကို သိမ္းဆည္းလိုပါက DhammaDownload အား Setting တြင္ Storage Permission ကို ခြင့္ျပဳေပးရန္ လိုအပ္ပါသည္။";

    public static final String SDCardStorageWarningMsg="External SD Card ေပၚတြင္သိမ္းဆည္းေသာ တရားေတာ္မ်ားသည္ DhammaDownload အား Uninstall လုပ္ပါက ေပ်ာက္ပ်က္သြားမည္ ျဖစ္သည္။";

    public static final String ShareTitle="တရားတော် မျှဝေရန်";


}
