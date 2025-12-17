import java.io.*;
import java.util.*;
import java.text.*;

class Route {
    String routeId;
    String source;
    String destination;
    double distanceKm;
    Route(String routeId, String source, String destination, double distanceKm) {
        this.routeId = routeId;
        this.source = source;
        this.destination = destination;
        this.distanceKm = distanceKm;
    }
}

class Shipment {
    String shipmentId;
    double actualWeightKg;
    double lengthCm;
    double widthCm;
    double heightCm;
    String routeId;
    static final double VOLUMETRIC_DIVISOR = 5000.0;
    Shipment(String shipmentId, double actualWeightKg, double lengthCm, double widthCm, double heightCm, String routeId) {
        this.shipmentId = shipmentId;
        this.actualWeightKg = actualWeightKg;
        this.lengthCm = lengthCm;
        this.widthCm = widthCm;
        this.heightCm = heightCm;
        this.routeId = routeId;
    }
    double volumetricWeightKg() {
        return (lengthCm * widthCm * heightCm) / VOLUMETRIC_DIVISOR;
    }
    double chargeableWeightKg() {
        return Math.max(actualWeightKg, volumetricWeightKg());
    }
}

class CostBreakdown {
    final String vehicleName;
    final double chargeableWeight;
    final double baseCost;
    final double fuelSurcharge;
    final double handlingFee;
    final double oversizeFee;
    final double total;
    CostBreakdown(String vehicleName, double chargeableWeight, double baseCost, double fuelSurcharge, double handlingFee, double oversizeFee) {
        this.vehicleName = vehicleName;
        this.chargeableWeight = chargeableWeight;
        this.baseCost = baseCost;
        this.fuelSurcharge = fuelSurcharge;
        this.handlingFee = handlingFee;
        this.oversizeFee = oversizeFee;
        this.total = baseCost + fuelSurcharge + handlingFee + oversizeFee;
    }
    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        DecimalFormat df2 = new DecimalFormat("#.00");
        return vehicleName + " | CHW: " + df2.format(chargeableWeight) + " kg | Base: ₹" + df.format(baseCost) + " | Fuel: ₹" + df.format(fuelSurcharge) + " | Handling: ₹" + df.format(handlingFee) + " | Oversize: ₹" + df.format(oversizeFee) + " | TOTAL: ₹" + df.format(total);
    }
}

abstract class Vehicle {
    String name;
    double capacityKg;
    double ratePerKgPerKm;
    double fuelSurchargePercent;
    double handlingFeeFlat;
    double oversizeThresholdCm;
    double oversizeFee;
    double minCharge;
    Vehicle(String name, double capacityKg, double ratePerKgPerKm, double fuelSurchargePercent, double handlingFeeFlat, double oversizeThresholdCm, double oversizeFee, double minCharge) {
        this.name = name;
        this.capacityKg = capacityKg;
        this.ratePerKgPerKm = ratePerKgPerKm;
        this.fuelSurchargePercent = fuelSurchargePercent;
        this.handlingFeeFlat = handlingFeeFlat;
        this.oversizeThresholdCm = oversizeThresholdCm;
        this.oversizeFee = oversizeFee;
        this.minCharge = minCharge;
    }
    CostBreakdown calculateCost(Shipment s, double distanceKm) {
        double chw = s.chargeableWeightKg();
        if (chw > capacityKg) throw new IllegalArgumentException(name + " cannot carry " + chw + " kg (capacity " + capacityKg + " kg).");
        double base = ratePerKgPerKm * chw * distanceKm;
        double fuel = base * fuelSurchargePercent;
        double handling = handlingFeeFlat;
        boolean oversize = s.lengthCm > oversizeThresholdCm || s.widthCm > oversizeThresholdCm || s.heightCm > oversizeThresholdCm;
        double oversizeCharge = oversize ? oversizeFee : 0.0;
        double subtotal = base + fuel + handling + oversizeCharge;
        if (subtotal < minCharge) {
            double diff = minCharge - subtotal;
            base += diff;
            fuel = base * fuelSurchargePercent;
            subtotal = base + fuel + handling + oversizeCharge;
        }
        fuel = base * fuelSurchargePercent;
        return new CostBreakdown(name, chw, base, fuel, handling, oversizeCharge);
    }
}

class GenericVehicle extends Vehicle {
    GenericVehicle(String name, double capacityKg, double ratePerKgPerKm, double fuelSurchargePercent, double handlingFeeFlat, double oversizeThresholdCm, double oversizeFee, double minCharge) {
        super(name, capacityKg, ratePerKgPerKm, fuelSurchargePercent, handlingFeeFlat, oversizeThresholdCm, oversizeFee, minCharge);
    }
}

class VehicleFactory {
    static Vehicle fromCsvRow(String vehicleType, double capacityKg, double ratePerKgPerKm, double fuelPct, double handling, double oversizeThreshold, double oversizeFee, double minCharge) {
        return new GenericVehicle(vehicleType, capacityKg, ratePerKgPerKm, fuelPct, handling, oversizeThreshold, oversizeFee, minCharge);
    }
}

class CreateCSV {
    static void createSamplesIfMissing() {
        try {
            File f1 = new File("routes.csv");
            File f2 = new File("fleet.csv");
            File f3 = new File("shipments.csv");
            if (f1.exists() && f2.exists() && f3.exists()) return;
            try (FileWriter w1 = new FileWriter("routes.csv");
                 FileWriter w2 = new FileWriter("fleet.csv");
                 FileWriter w3 = new FileWriter("shipments.csv")) {
                w1.write("routeId,source,destination,distanceKm\n");
                w1.write("R001,Vijayawada,Srikakulam,448\n");
                w1.write("R002,Vijayawada,Vizianagaram,398\n");
                w1.write("R003,Vijayawada,Visakhapatnam,361\n");
                w1.write("R004,Vijayawada,Anakapalli,314\n");
                w1.write("R005,Vijayawada,Parvathipuram Manyam,488\n");
                w1.write("R006,Vijayawada,Alluri Sitharama Raju,252\n");
                w1.write("R007,Vijayawada,Bapatla,84\n");
                w1.write("R008,Vijayawada,Dr. B.R. Ambedkar Konaseema (Amalapuram),182\n");
                w1.write("R009,Vijayawada,Kakinada,216\n");
                w1.write("R010,Vijayawada,Eluru,60\n");
                w1.write("R011,Vijayawada,Guntur,40\n");
                w1.write("R012,Vijayawada,Rajamundry,159\n");
                w1.write("R013,Vijayawada,Krishna (Machilipatnam),67\n");
                w1.write("R014,Vijayawada,Palnadu (Narasaraopet),96\n");
                w1.write("R015,Vijayawada,Prakasam (Ongole),155\n");
                w1.write("R016,Vijayawada,Sri Potti Sriramulu Nellore (Nellore),279\n");
                w1.write("R017,Vijayawada,West Godavari (Bhimavaram),138\n");
                w1.write("R018,Vijayawada,Ananthapuramu (Anantapur),482\n");
                w1.write("R019,Vijayawada,Annamayya (Rayachoti),436\n");
                w1.write("R020,Vijayawada,Chittoor,483\n");
                w1.write("R021,Vijayawada,YSR Kadapa (Kadapa),387\n");
                w1.write("R022,Vijayawada,Kurnool,348\n");
                w1.write("R023,Vijayawada,Nandyal,330\n");
                w1.write("R024,Vijayawada,Sri Sathya Sai (Puttaparthi area),522\n");
                w1.write("R025,Vijayawada,Tirupati,417\n");
                w1.write("R026,Vijayawada,Hyderabad,277\n");
                w1.write("R027,Vijayawada,Bengaluru,663\n");
                w1.write("R028,Vijayawada,Chennai,455\n");
                w1.write("R029,Vijayawada,Mumbai,1025\n");
                w1.write("R030,Vijayawada,New Delhi,1985\n");
                w1.write("R031,Vijayawada,Kolkata,1215\n");
                w1.write("R032,Vijayawada,Pune,842\n");
                w1.write("R033,Vijayawada,Ahmedabad,1471\n");
                w1.write("R034,Vijayawada,Bhubaneswar,774\n");
                w1.write("R035,Vijayawada,Thiruvananthapuram,1181\n");
                w1.write("R036,Vijayawada,Kochi,1094\n");
                w2.write("vehicleType,capacityKg,ratePerKgPerKm,fuelPct,handling,oversizeThresholdCm,oversizeFee,minCharge\n");
                w2.write("Truck,10000,0.12,0.12,500,250,2500,5000\n");
                w2.write("Van,1500,0.20,0.10,200,180,1000,800\n");
                w2.write("Bike,30,0.80,0.05,50,60,200,100\n");
                w3.write("shipmentId,actualKg,lengthCm,widthCm,heightCm,routeId\n");
                w3.write("S1,14,60,40,30,R001\n");
                w3.write("S2,2,20,15,10,R002\n");
                w3.write("S3,800,200,120,150,R003\n");
            }
        } catch (IOException e) {
            System.out.println("Error creating sample CSVs: " + e.getMessage());
        }
    }
}

class CsvUtils {
    static List<String[]> readAll(String path) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split(",", -1);
                for (int i = 0; i < cols.length; i++) cols[i] = cols[i].trim();
                rows.add(cols);
            }
        }
        return rows;
    }
}

public class TransportAndLogistics {

    public static Map<String, Route> readRoutes(String path) throws Exception {
        Map<String, Route> map = new LinkedHashMap<>();
        List<String[]> rows = CsvUtils.readAll(path);
        for (String[] t : rows) {
            if (t.length < 4) continue;
            String id = t[0];
            String src = t[1];
            String dst = t[2];
            double dist = 0;
            try { dist = Double.parseDouble(t[3]); } catch (Exception ignored) {}
            map.put(id, new Route(id, src, dst, dist));
        }
        return map;
    }

    public static List<Vehicle> readFleet(String path) throws Exception {
        List<Vehicle> list = new ArrayList<>();
        List<String[]> rows = CsvUtils.readAll(path);
        for (String[] t : rows) {
            if (t.length < 8) continue;
            String vehicleType = t[0];
            double cap = Double.parseDouble(t[1]);
            double rate = Double.parseDouble(t[2]);
            double fuel = Double.parseDouble(t[3]);
            double handling = Double.parseDouble(t[4]);
            double oversizeTh = Double.parseDouble(t[5]);
            double oversizeFee = Double.parseDouble(t[6]);
            double minCharge = Double.parseDouble(t[7]);
            list.add(VehicleFactory.fromCsvRow(vehicleType, cap, rate, fuel, handling, oversizeTh, oversizeFee, minCharge));
        }
        return list;
    }

    public static List<Shipment> readShipments(String path) throws Exception {
        List<Shipment> list = new ArrayList<>();
        List<String[]> rows = CsvUtils.readAll(path);
        for (String[] t : rows) {
            if (t.length < 6) continue;
            String sid = t[0];
            double actual = Double.parseDouble(t[1]);
            double l = Double.parseDouble(t[2]);
            double w = Double.parseDouble(t[3]);
            double h = Double.parseDouble(t[4]);
            String routeId = t[5];
            list.add(new Shipment(sid, actual, l, w, h, routeId));
        }
        return list;
    }

    public static void main(String[] args) {
        CreateCSV.createSamplesIfMissing();
        Scanner sc = new Scanner(System.in);
        Map<String, Route> routes;
        List<Vehicle> fleet;
        try {
            routes = readRoutes("routes.csv");
            fleet = readFleet("fleet.csv");
        } catch (Exception e) {
            System.out.println("Error loading CSVs: " + e.getMessage());
            return;
        }
        while (true) {
            System.out.println("1. List Vehicles");
            System.out.println("2. List Routes");
            System.out.println("3. Calculate Cost");
            System.out.println("4. Load Shipments and Show Best Options");
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");
            String choiceLine = sc.nextLine().trim();
            int choice;
            try { choice = Integer.parseInt(choiceLine); } catch (Exception e) { System.out.println("Invalid input"); continue; }
            switch (choice) {
                case 1:
                    for (int i = 0; i < fleet.size(); i++) {
                        System.out.println((i + 1) + ". " + fleet.get(i).name);
                    }
                    break;
                case 2:
                    for (Route r : routes.values()) {
                        System.out.println(r.routeId + ": " + r.source + " -> " + r.destination + " (" + r.distanceKm + " km)");
                    }
                    break;
                case 3:
                    if (fleet.isEmpty() || routes.isEmpty()) { System.out.println("Fleet or routes not loaded"); break; }
                    System.out.println("Select Vehicle:");
                    for (int i = 0; i < fleet.size(); i++) System.out.println((i + 1) + ". " + fleet.get(i).name);
                    System.out.print("Enter number: ");
                    int vChoice;
                    try { vChoice = Integer.parseInt(sc.nextLine().trim()); } catch (Exception e) { System.out.println("Invalid"); break; }
                    if (vChoice < 1 || vChoice > fleet.size()) { System.out.println("Invalid vehicle"); break; }
                    Vehicle selectedVehicle = fleet.get(vChoice - 1);
                    System.out.print("Enter Route ID: ");
                    String routeId = sc.nextLine().trim();
                    Route selectedRoute = routes.get(routeId);
                    if (selectedRoute == null) { System.out.println("Invalid route"); break; }
                    try {
                        System.out.print("Enter actual weight (kg): ");
                        double actual = Double.parseDouble(sc.nextLine().trim());
                        System.out.print("Enter length (cm): ");
                        double L = Double.parseDouble(sc.nextLine().trim());
                        System.out.print("Enter width (cm): ");
                        double W = Double.parseDouble(sc.nextLine().trim());
                        System.out.print("Enter height (cm): ");
                        double H = Double.parseDouble(sc.nextLine().trim());
                        Shipment shipment = new Shipment("S", actual, L, W, H, routeId);
                        CostBreakdown cb = selectedVehicle.calculateCost(shipment, selectedRoute.distanceKm);
                        System.out.println(cb);
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                case 4:
                    try {
                        List<Shipment> shipments = readShipments("shipments.csv");
                        for (Shipment s : shipments) {
                            Route r = routes.get(s.routeId);
                            if (r == null) { System.out.println("Shipment " + s.shipmentId + " references unknown route " + s.routeId); continue; }
                            System.out.println("Shipment " + s.shipmentId + " route " + r.routeId + " (" + r.distanceKm + " km)");
                            CostBreakdown best = null;
                            for (Vehicle v : fleet) {
                                try {
                                    CostBreakdown cb = v.calculateCost(s, r.distanceKm);
                                    System.out.println("  " + cb);
                                    if (best == null || cb.total < best.total) best = cb;
                                } catch (Exception ignored) {}
                            }
                            if (best != null) System.out.println(" => Best: " + best.vehicleName + " at ₹" + new DecimalFormat("#,##0.00").format(best.total));
                            else System.out.println(" => No suitable vehicle");
                        }
                    } catch (Exception e) {
                        System.out.println("Error reading shipments: " + e.getMessage());
                    }
                    break;
                case 5:
                    System.out.println("Exiting");
                    return;
                default:
                    System.out.println("Invalid choice");
            }
        }
    }
}