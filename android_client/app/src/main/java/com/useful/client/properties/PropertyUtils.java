package com.useful.client.properties;

import android.app.Application;
import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PropertyUtils {

    private static PropertyUtils instance = null;

    private Application application = null;

    private JSONObject jsonRoot = null;

    public static PropertyUtils getInstance() {
        return instance;
    }

    public PropertyUtils(Application application) {
        this.application = application;
        this.instance = this;

        initialize();
    }

    private void initialize() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(application.getAssets().open("config.json")));

            String line = "", totals = "";
            while ((line = br.readLine()) != null) {
                totals += line;
            }

            jsonRoot = (JSONObject) JSON.parse(totals);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public String getString(String key) {
        if (jsonRoot == null) {
            return null;
        }

        JSONObject jsonObject = jsonRoot;
        String[] nodeNames = key.split("\\.");
        for (int idx = 0; idx < nodeNames.length; idx++) {
            String nodeName = nodeNames[idx];
            if (idx == nodeNames.length - 1) {
                return jsonObject.getString(nodeName);
            }

            jsonObject = jsonObject.getJSONObject(nodeName);
        }
        return null;
    }

    public Integer getInteger(String key) {
        String value = getString(key);
        if (value == null) {
            return null;
        }

        return Integer.parseInt(value.trim());
    }

    public Boolean getBoolean(String key) {
        String value = getString(key);
        if (value == null) {
            return null;
        }

        return Boolean.parseBoolean(value.trim());
    }
}
