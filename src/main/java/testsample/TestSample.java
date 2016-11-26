package testsample;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.format.geojson.TLcdGeoJsonModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.imaging.multispectral.general.GeneralOperationPanel;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by luciad on 26.11.16.
 */
public class TestSample extends LightspeedSample {

    @Override
    protected void addData() throws IOException {
        LspDataUtil.instance().grid().addToView(getView());
        TLcdGeoJsonModelDecoder modelDecoder = new TLcdGeoJsonModelDecoder();
        ILcdModel malasya = modelDecoder.decode("malasia_cleaned.geojson");

        MalasyiaStyler malasyastyler = new MalasyiaStyler();
        ILspInteractivePaintableLayer layer = TLspShapeLayerBuilder.newBuilder().model(malasya).bodyStyler(TLspPaintState.REGULAR, malasyastyler).build();
        getView().addLayer(layer);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                malasyastyler.setTime(malasyastyler.getTime() + (1000 * 60 * 60 * 24));
            }
        }, 0, 50);
    }

    public static void main(String[] args) {
        LightspeedSample.startSample(TestSample.class, "test sample");
    }

    private static class MalasyiaStyler extends ALspStyler {

        private long time = 1357167600000l;

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            System.out.println(time);
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
}
