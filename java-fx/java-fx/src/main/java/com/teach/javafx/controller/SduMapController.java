package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;

public class SduMapController extends ToolController {
    @FXML
    private WebView webView;

    @FXML
    public void initialize() {
        webView.getEngine().loadContent(getMapHtml());
    }

    private String getMapHtml() {
        String apiKey = "3976b7fa0b4813c868d01165a8b39a8f";
        String securityJsCode = "6f81757d58d15edfd996b6a98550ef55";

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <style>\n" +
                "        html, body, #container { height: 100%; margin: 0; padding: 0; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id='container'></div>\n" +
                "    <script type=\"text/javascript\">\n" +
                "        window._AMapSecurityConfig = {\n" +
                "            securityJsCode: '" + securityJsCode + "'\n" +
                "        };\n" +
                "    </script>\n" +
                "    <script src=\"https://webapi.amap.com/maps?v=2.0&key=" + apiKey + "\"></script>\n" +
                "    <script>\n" +
                "    if (window.devicePixelRatio < 3) {\n" +
                "        window.devicePixelRatio = 3;\n" +
                "    }\n"+
                "        window.map = new AMap.Map('container', {\n" +
                "            center: [117.068511, 36.652580],\n" +
                "            zoom: 13,\n" +
                "            viewMode: '2D'," +
                "            renderer: 'canvas'" +
                "        });\n" +
                "        const campuses = [\n" +
                "            {name:'中心校区', lng:117.060171, lat:36.675538},\n" +
                "            {name:'洪家楼校区', lng:117.068511, lat:36.687334},\n" +
                "            {name:'趵突泉校区', lng:117.018500, lat:36.652580},\n" +
                "            {name:'千佛山校区', lng:117.028168, lat:36.649790},\n" +
                "            {name:'软件园校区', lng:117.139052, lat:36.666811},\n" +
                "            {name:'兴隆山校区', lng:117.050000, lat:36.599470},\n" +
                "        ];\n" +
                "        campuses.forEach(function(c) {\n" +
                "            const marker = new AMap.Marker({\n" +
                "                position: [c.lng, c.lat],\n" +
                "                title: c.name\n" +
                "            });\n" +
                "            const label = new AMap.Text({\n" +
                "                position: [c.lng, c.lat],\n" +
                "                text: c.name,\n" +
                "                offset: new AMap.Pixel(0, -30),\n" +
                "                style: { 'font-size': '12px',\n" +
                "        'color': '#fff',\n" +
                "        'background': '#1890ff',\n" +
                "        'padding': '3px 8px',\n" +
                "        'border-radius': '4px',\n" +
                "        'border': 'none',\n" +
                "        'box-shadow': '0 2px 4px rgba(0,0,0,0.3)'}\n" +
                "            });\n" +
                "            window.map.add(marker);\n" +
                "            window.map.add(label);\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

    private void moveToCampus(double lng, double lat) {
        webView.getEngine().executeScript(
                "if (typeof window.map !== 'undefined') {" +
                        "   window.map.setCenter([" + lng + ", " + lat + "]);" +
                        "   window.map.setZoom(17);" +
                        "} else {" +
                        "   alert('地图尚未加载完成，请稍后再试');" +
                        "}"
        );
    }

    @FXML private void onCampusCenterClick() { moveToCampus(117.060171, 36.675538); }
    @FXML private void onCampusHongjialouClick() { moveToCampus(117.068511, 36.687334); }
    @FXML private void onCampusBaotuquanClick() { moveToCampus(117.018500, 36.652580); }
    @FXML private void onCampusQianfoshanClick() { moveToCampus(117.028168, 36.649790); }
    @FXML private void onCampusSoftwareClick() { moveToCampus(117.139052, 36.666811); }
    @FXML private void onCampusXinglongshanClick() { moveToCampus(117.050000, 36.599470); }
}
