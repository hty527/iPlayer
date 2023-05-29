package com.android.player.danmu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.android.JSONSource;
import master.flame.danmaku.danmaku.util.DanmakuUtils;

/**
 * created by hty
 * 2022/6/28
 * Desc:弹幕构造器
 */
public class DanmuParser extends BaseDanmakuParser {

    public DanmuParser() {

    }

    public Danmakus parse() {
        if (this.mDataSource != null && this.mDataSource instanceof JSONSource) {
            JSONSource jsonSource = (JSONSource) this.mDataSource;
            return this.doParse(jsonSource.data());
        } else {
            return new Danmakus();
        }
    }

    private Danmakus doParse(JSONArray danmakuListData) {
        Danmakus danmakus = new Danmakus();
        if (danmakuListData != null && danmakuListData.length() != 0) {
            for (int i = 0; i < danmakuListData.length(); ++i) {
                try {
                    JSONObject e = danmakuListData.getJSONObject(i);
                    if (e != null) {
                        danmakus = this._parse(e, danmakus);
                    }
                } catch (JSONException var5) {
                    var5.printStackTrace();
                }
            }

            return danmakus;
        } else {
            return danmakus;
        }
    }

    private Danmakus _parse(JSONObject jsonObject, Danmakus danmakus) {
        if (danmakus == null) {
            danmakus = new Danmakus();
        }

        if (jsonObject != null && jsonObject.length() != 0) {
            for (int i = 0; i < jsonObject.length(); ++i) {
                try {
                    String c = jsonObject.getString("c");
                    String[] values = c.split(",");
                    if (values.length > 0) {
                        int type = Integer.parseInt(values[2]);
                        if (type != 7) {
                            long time = (long) (Float.parseFloat(values[0]) * 1000.0F);
                            int color = Integer.parseInt(values[1]) | -16777216;
                            float textSize = Float.parseFloat(values[3]);
                            BaseDanmaku item = this.mContext.mDanmakuFactory.createDanmaku(type, this.mContext);
                            if (item != null) {
                                item.setTime(time);
                                item.textSize = textSize * (this.mDispDensity - 0.6F);
                                item.textColor = color;
                                item.textShadowColor = color <= -16777216 ? -1 : -16777216;
                                DanmakuUtils.fillText(item, jsonObject.optString("m", "...."));
                                item.index = i;
                                item.setTimer(this.mTimer);
                                danmakus.addItem(item);
                            }
                        }
                    }
                } catch (JSONException var13) {

                } catch (NumberFormatException var14) {

                }
            }

            return danmakus;
        } else {
            return danmakus;
        }
    }
}