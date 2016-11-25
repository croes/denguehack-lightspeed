package com.mycompany.app;

import com.luciad.datamodel.*;
import com.luciad.format.geojson.TLcdFeatureMetaDataProvider;
import com.luciad.format.geojson.TLcdGeoJsonModelEncoder;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.TLcdShapeList;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.mycompany.app.csv.CSVModelDecoder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Hello world!
 */
public class App {

    private static final long DISEASE_DURATION = 1000 * 60 * 60 * 24 * 14;

    public static void main(String[] args) throws IOException {
        TLcdSHPModelDecoder decoder = new TLcdSHPModelDecoder();
        ILcdModel stateModel = decoder.decode("/home/luciad/data/dengue/malaysia/spatial_data/MYS_adm2.shp");
        Enumeration elements = stateModel.elements();

        HashMap<String, ILcdDataObject> stateMap = new HashMap<>();
        while (elements.hasMoreElements()) {
            ILcdDataObject o = (ILcdDataObject) elements.nextElement();
            Object hasc_1 = o.getValue("HASC_2");
            stateMap.put((String) hasc_1, o);
        }

        CSVModelDecoder csvModelDecoder = new CSVModelDecoder();
        ILcdModel decode = csvModelDecoder.decode("/home/luciad/data/dengue/malaysia/data/MYS_HealthMap_DengueCases.csv");
        Enumeration elements1 = decode.elements();

        ILcdModel geojsonModel = new TLcdVectorModel(new TLcdGeodeticReference(), new TLcdModelDescriptor());

        TLcdGeoJsonModelEncoder modelEncoder = new TLcdGeoJsonModelEncoder();
        TLcdDataModelBuilder stateModelBuilder = new TLcdDataModelBuilder("StateModel");
        TLcdDataTypeBuilder stateTypeBuilder = stateModelBuilder.typeBuilder("StateType");
        stateTypeBuilder.addProperty("cases", "CaseType").collectionType(TLcdDataProperty.CollectionType.LIST);
        TLcdDataTypeBuilder caseTypeBuilder = stateModelBuilder.typeBuilder("CaseType");
        caseTypeBuilder.addProperty("startDate", TLcdCoreDataTypes.LONG_TYPE);
        caseTypeBuilder.addProperty("endDate", TLcdCoreDataTypes.LONG_TYPE);
        caseTypeBuilder.addProperty("cumCount", TLcdCoreDataTypes.LONG_TYPE);
        TLcdDataModel dataModel = stateModelBuilder.createDataModel();
        TLcdDataType stateType = dataModel.getDeclaredType("StateType");
        TLcdDataType caseType = dataModel.getDeclaredType("CaseType");

        HashMap<ILcdDataObject, List<CaseRepresentation>> stateToCases = new HashMap<>();
        while (elements1.hasMoreElements()) {
            ILcdDataObject state;
            ILcdDataObject o = (ILcdDataObject) elements1.nextElement();
            String hasc_2 = (String) o.getValue("HASC_2");
            if (hasc_2 == null || hasc_2.isEmpty()) {
                Double lon = Double.parseDouble((String) o.getValue("Lon"));
                Double lat = Double.parseDouble((String) o.getValue("Lat"));
                TLcdLonLatPoint point = new TLcdLonLatPoint(lon, lat);
                state = findState(stateModel, point);
                if (state == null) {
                    System.out.println("NEED TO GEOLOCATE POINT " + point + " " + o.getValue("Location"));
                    continue;
                }
            } else {
                state = stateMap.get(hasc_2);
                if (state == null) {
                    System.out.println("COULD NOT FIND STATE WITH CODE: " + hasc_2);
                    continue;
                }
            }

            String location = (String) o.getValue("Location");
            String dateString = (String) o.getValue("IssueDateFormat");
            Date date = null;
            try {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                date = format.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String cumCount = (String) o.getValue("CumConfirmedCases");
            System.out.println("FOUND REPORT AT " + location + " FOR DATE " + dateString + " WITH CUMCOUNT " + cumCount);

            try {
                CaseRepresentation caseRepresentation = new CaseRepresentation(caseType, date.getTime(), date.getTime(), Long.parseLong(cumCount));

                List<CaseRepresentation> caseRepresentations = stateToCases.get(state);
                if (caseRepresentations == null) {
                    caseRepresentations = new ArrayList<>();
                    stateToCases.put(state, caseRepresentations);
                }
                caseRepresentations.add(caseRepresentation);

            } catch (NumberFormatException e) {
                System.out.println("Skipping bad entry: " + location + " FOR DATE " + dateString + " WITH CUMCOUNT " + cumCount);
            }
        }

        for (Map.Entry<ILcdDataObject, List<CaseRepresentation>> entry : stateToCases.entrySet()) {
            List<CaseRepresentation> caseList = entry.getValue();
            caseList.sort(Comparator.comparing(CaseRepresentation::getStartDate));

            long previousValue = 0;
            if (!caseList.isEmpty()) {
                CaseRepresentation initialCase = caseList.get(0);
                initialCase.setValue("endDate", initialCase.getStartDate() + DISEASE_DURATION);
                previousValue = initialCase.getCumCount();
            }
            for (int i = 1; i < caseList.size(); i++) {
                CaseRepresentation caseRepresentation = caseList.get(i);
                caseRepresentation.setValue("endDate", caseRepresentation.getStartDate() + DISEASE_DURATION);
                long tempValue = caseRepresentation.getCumCount();
                long diff = caseRepresentation.getCumCount() - previousValue;
                if (diff >= 0) {
                    caseRepresentation.setValue("cumCount", diff);
                }
                previousValue = tempValue;
            }
        }

        for (Map.Entry<ILcdDataObject, List<CaseRepresentation>> entry : stateToCases.entrySet()) {
            geojsonModel.addElement(new StateDomainObject(entry.getValue(), (ILcdShape) entry.getKey(), stateType), ILcdModel.NO_EVENT);
        }

        StateFeatureMetadataEncoder metadataEncoder = new StateFeatureMetadataEncoder();
        modelEncoder.setFeatureMetaDataProvider(metadataEncoder);
        modelEncoder.export(geojsonModel, "/tmp/output.geojson");
    }

    private static ILcdDataObject findState(ILcdModel stateModel, ILcdPoint point) {
        Enumeration elements = stateModel.elements();
        while (elements.hasMoreElements()) {
            ILcdShape state = (ILcdShape) elements.nextElement();
            if (state.contains2D(point)) {
                return (ILcdDataObject) state;
            }
        }
        return null;

    }

    private static class StateFeatureMetadataEncoder extends TLcdFeatureMetaDataProvider {
        private int id = 0;

        public StateFeatureMetadataEncoder() {
        }

        @Override
        public String getIdPropertyName(Object aObject) {
            return "id";
        }

        @Override
        public Set<String> getPropertyNames(Object aDomainObject) {
            if (aDomainObject instanceof StateDomainObject) {
                return new HashSet<>(Arrays.asList("countType"));
            } else {
                return super.getPropertyNames(aDomainObject);
            }
        }

        @Override
        public Object getPropertyValue(Object aDomainObject, String aPropertyName) {
            if (aDomainObject instanceof StateDomainObject) {
                switch (aPropertyName) {
                    case "id":
                        return id++;
                    case "countType":
                        return ((StateDomainObject) aDomainObject).getCaseRepresentation();
                    default:
                        throw new IllegalArgumentException("PANIC3");
                }
            } else {
                return super.getPropertyValue(aDomainObject, aPropertyName);
            }
        }

    }

    private static class CaseRepresentation extends TLcdDataObject {

        public CaseRepresentation(TLcdDataType aDataType, long startDate, long endDate, long cumCount) {
            super(aDataType);
            setValue("startDate", startDate);
            setValue("endDate", endDate);
            setValue("cumCount", cumCount);
        }

        private long getStartDate() {
            return (Long) getValue("startDate");
        }

        private long getCumCount() {
            return (Long) getValue("cumCount");
        }

    }

    private static class StateDomainObject extends TLcdShapeList implements ILcdDataObject {

        private List<CaseRepresentation> fCaseRepresentation;

        private TLcdDataType fDataType;

        public StateDomainObject(List<CaseRepresentation> caseRepresentation, ILcdShape shape, TLcdDataType aDataType) {
            this.fCaseRepresentation = caseRepresentation;
            this.fDataType = aDataType;
            this.addShape(shape);
        }

        public List<CaseRepresentation> getCaseRepresentation() {
            return fCaseRepresentation;
        }

        public StateDomainObject() {

        }

        @Override
        public TLcdDataType getDataType() {
            return fDataType;
        }

        @Override
        public Object getValue(TLcdDataProperty tLcdDataProperty) {
            return null;
        }

        @Override
        public Object getValue(String s) {
            return null;
        }

        @Override
        public void setValue(TLcdDataProperty tLcdDataProperty, Object o) {

        }

        @Override
        public void setValue(String s, Object o) {
        }

        @Override
        public boolean hasValue(TLcdDataProperty tLcdDataProperty) {
            return true;
        }

        @Override
        public boolean hasValue(String s) {
            return true;
        }
    }
}
