package com.example.gmapdemo;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectionParse {
    public List<List<HashMap<String,String>>> parse(JSONObject jsonObject)
    {
        List<List<HashMap<String,String>>> routes = new ArrayList<>();
        JSONArray Jroutes = null;
        JSONArray Jlegs = null;
        JSONArray Jsteps = null;
        try {
            Jroutes = jsonObject.getJSONArray("routes");

            //Loop for All Routes
            for(int i = 0;i<Jroutes.length();i++)
            {
                Jlegs = ((JSONObject) Jroutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String,String>>();

                //Loop for all Leg routes
                for(int j = 0;i<Jlegs.length();j++)
                {
                    Jsteps = ((JSONObject) Jlegs.get(j)).getJSONArray("steps");

                    //Loop for ALL points
                    for(int k=0;k<Jsteps.length();k++)
                    {
                        String polyline = "";
                        polyline = (String) ((JSONObject)((JSONObject)Jsteps.get(k)).get("polyline")).get("points");
                        List list = decodePolyline(polyline);

                        //Loop for ALL points
                        for(int l=0;l<list.size();l++)
                        {
                            HashMap<String,String > hashMap = new HashMap<>();
                            hashMap.put("lat",Double.toString( ( (LatLng) list.get(l) ).latitude) );
                            hashMap.put("lon",Double.toString( ( (LatLng) list.get(l) ).longitude) );
                            path.add(hashMap);
                        }
                    }
                    routes.add(path);

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return routes;
    }

    private List decodePolyline(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
