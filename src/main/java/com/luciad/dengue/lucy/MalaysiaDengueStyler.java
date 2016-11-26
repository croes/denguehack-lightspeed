package com.luciad.dengue.lucy;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MalaysiaDengueStyler extends ALspStyler {

    private long time = 1357167600000l;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
        fireStyleChangeEvent();
    }

    @Override
    public void style(Collection<?> collection, ALspStyleCollector styleCollector, TLspContext context) {
        for (Object o : collection) {
            List<HashMap<String, Object>> countType = (List<HashMap<String, Object>>) ((ILcdDataObject) o).getValue("countType");
            long count = 0;
            for (HashMap<String, Object> dataObject : countType) {
                Integer cumCount = (Integer) dataObject.get("cumCount");
                Long startDate = (Long) dataObject.get("startDate");
                Long endDate = (Long) dataObject.get("endDate");
                if (time > startDate && time <= endDate) {
                    count += cumCount;
                }

            }
            Color color = null;
            if (count < 5) {
                color = new Color(255, 255, 178, 192);
            } else if (count < 10) {
                color = new Color(254, 217, 118, 192);
            } else if (count < 20) {
                color = new Color(254, 178, 76, 192);
            } else if (count < 50) {
                color = new Color(253, 141, 60, 192);
            } else if (count < 100) {
                color = new Color(252, 78, 42, 192);
            } else if (count < 500) {
                color = new Color(227, 26, 28, 192);
            } else {
                color = new Color(177, 0, 38, 192);
            }
            styleCollector.object(o).style(TLspFillStyle.newBuilder().color(color).build()).submit();
        }
    }
}