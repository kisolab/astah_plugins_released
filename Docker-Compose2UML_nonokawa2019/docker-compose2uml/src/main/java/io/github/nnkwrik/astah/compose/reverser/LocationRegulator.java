package io.github.nnkwrik.astah.compose.reverser;

import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import io.github.nnkwrik.astah.model.Network;
import io.github.nnkwrik.astah.model.Service;
import lombok.Data;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 依存に応じてserviceの場所を扇円に調節
 *
 * @author Reika Nonokawa
 */
public class LocationRegulator {

    private Map<Service, List<IElement>> serviceElements;

    private Map<Network, Long> networkUsageCount;

    private double maxWidth;
    private double maxHeight;

    public LocationRegulator(Map<Service, List<IElement>> serviceElements, Map<Network, Long> networkUsageCount) {
        this.serviceElements = serviceElements;
        this.networkUsageCount = networkUsageCount;
        Set<IElement> elementSet = serviceElements.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        setMaxLength(elementSet);
    }

    private void setMaxLength(Set<IElement> serviceElements) {
        for (IElement element : serviceElements) {
            try {
                INodePresentation presentation = (INodePresentation) element.getPresentations()[0];
                maxHeight = Math.max(maxHeight, presentation.getHeight());
                maxWidth = Math.max(maxWidth, presentation.getWidth());
            } catch (InvalidUsingException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 依存とネットワークに応じて場所決め、serviceの場所を扇円に調節
     * @throws InvalidUsingException
     * @throws InvalidEditingException
     */
    public void adjust() throws InvalidUsingException, InvalidEditingException {
        Map<Service, Point2DWrapper> serviceLocation = new HashMap<>();
        serviceElements.keySet().stream()
                //依存されている数順にサービスを並び替え
                .sorted((o1, o2) -> {
                    Integer size1 = o1.getDependsOn() == null ? 0 : o1.getDependsOn().size();
                    Integer size2 = o2.getDependsOn() == null ? 0 : o2.getDependsOn().size();
                    return size2.compareTo(size1);
                })
                .forEach(s -> {
                    if (!serviceLocation.containsKey(s)) {
                        serviceLocation.put(s, nextLocation());
                        doAdjust(s, serviceLocation);
                    }
                });

        adjust4Network(serviceLocation);

        for (Map.Entry<Service, Point2DWrapper> entry : serviceLocation.entrySet()) {
            Service service = entry.getKey();
            Point2D newLocation = entry.getValue();
            moveElements(serviceElements.get(service), newLocation);
        }

    }

    /**
     * 指定された場所に、serviceの場所を扇円に調節
     * @param locationMap
     * @throws InvalidUsingException
     * @throws InvalidEditingException
     */
    public void adjust(Map<Service,Point2D> locationMap) throws InvalidUsingException, InvalidEditingException {
        for (Map.Entry<Service, Point2D> entry : locationMap.entrySet()) {
            Service service = entry.getKey();
            Point2D newLocation = entry.getValue();
            moveElements(serviceElements.get(service), newLocation);
        }

    }

    /**
     * 深さ優先順で、依存を多く持っているサービスから位置を決める。
     * @param lastAdjust
     * @param serviceLocation
     */
    private void doAdjust(Service lastAdjust, Map<Service, Point2DWrapper> serviceLocation) {
        //最も依存を持っているサービス
        Optional<Service> maxDependsOp = lastAdjust.getDependsOrLinksService().keySet().stream()
                .filter(s -> !serviceLocation.containsKey(s)) //すでに位置を決めてあるか
                .sorted((o1, o2) -> {
                    Integer size1 = o1.getDependsOn() == null ? 0 : o1.getDependsOn().size();
                    Integer size2 = o2.getDependsOn() == null ? 0 : o2.getDependsOn().size();
                    return size2.compareTo(size1);
                })
                .findFirst();
        if (!maxDependsOp.isPresent()) return;
        Service maxDepends = maxDependsOp.get();
        //位置を決める
        serviceLocation.put(maxDepends, nextLocation());
        //最も依存を持っているサービスについて、再帰
        doAdjust(maxDepends, serviceLocation);
    }

    /**
     * 同じネットワークのサービスを、扇円の同じ縦ブロックに配置する
     * @param serviceLocation
     */
    private void adjust4Network(Map<Service, Point2DWrapper> serviceLocation) {
        Map<Integer, Set<Service>> levelServiceMap = serviceLocation.entrySet().stream()
                .collect(Collectors.groupingBy(e -> e.getValue().getLevel(),
                        Collectors.mapping(e -> e.getKey(), Collectors.toSet())));

        Map<Service, List<Network>> relateNetwork =
                NetworksHelper.relateNetwork(serviceLocation.keySet(), networkUsageCount);

        for (Set<Service> ls : levelServiceMap.values()) {

            //serviceと関係のあるネットワークのうち最も使われないネットワーク
            Map<Service, Network> networkMinUsage = ls.stream()
                    .collect(Collectors.toMap(
                            s -> s,
                            s -> relateNetwork.get(s).stream()
                                    .distinct()
                                    .min(Comparator.comparing(n -> networkUsageCount.get(n)))
                                    .orElse(Network.defaultNetwork)
                    ));


            List<Service> sortedServices = networkMinUsage.entrySet().stream()
                    .sorted((e1, e2) -> {
                        Network net1 = e1.getValue();
                        Network net2 = e2.getValue();
                        if (net1.equals(Network.defaultNetwork) && !net2.equals(Network.defaultNetwork)){
                            return -1;
                        }else if (!net1.equals(Network.defaultNetwork) && net2.equals(Network.defaultNetwork)){
                            return 1;
                        }
                        else if (net1 != net2) {
                            //使われる回数の少ないネットワークを持っているサービスほど前
                            return networkUsageCount.get(net1).compareTo(networkUsageCount.get(net2));
                        } else {
                            //比率が大きいほど前
                            List<Network> nets1 = relateNetwork.get(e1.getKey());
                            List<Network> nets2 = relateNetwork.get(e2.getKey());
                            double r1 = nets1.stream().filter(n -> n == net1).count() / (double) nets1.size();
                            double r2 = nets2.stream().filter(n -> n == net2).count() / (double) nets2.size();
                            return Double.compare(r2, r1);
                        }
                    })
                    .map(e -> e.getKey())
                    .collect(Collectors.toList());

            List<Point2DWrapper> sortedPoints = sortedServices.stream()
                    .map(s -> serviceLocation.get(s))
                    .sorted(Comparator.comparingDouble(point -> point.x))
                    .collect(Collectors.toList());

            for (int i = 0; i < sortedServices.size(); i++) {
                serviceLocation.put(sortedServices.get(i), sortedPoints.get(i));
            }
        }
    }

    /**
     * newLocationの位置に移動する
     * @param elements 移動したいastah要素
     * @param newLocation 移動先の場所
     * @throws InvalidUsingException
     * @throws InvalidEditingException
     */
    private void moveElements(List<IElement> elements, Point2D newLocation) throws InvalidUsingException, InvalidEditingException {
        for (IElement element : elements) {
            INodePresentation presentation = (INodePresentation) element.getPresentations()[0];
            Point2D location = presentation.getLocation();
            double x = location.getX() + newLocation.getX();
            double y = location.getY() + newLocation.getY();
            location.setLocation(x, y);
            presentation.setLocation(location);
        }
    }

    //半径
    private double radius = 50;
    //列
    private int offset = 0;
    //行
    private int level = 0;
    //角度
    private double arg = 0.0d;

    /**
     * 扇円での次の座標を計算する
     * @return
     */
    private Point2DWrapper nextLocation() {
        double interval = maxWidth + 100;
        double perimeter = 0.7 * radius * Math.PI;//　1/3　circle

        if (interval * offset > perimeter) {
            radius += maxHeight + 150;
            offset = 0;
//            arg = Math.PI / 4 * random.nextDouble() - Math.PI / 8;//-PI/8 ~ +PI/8
            arg = (arg + Math.PI / 19) % 2 * Math.PI / 8 - Math.PI / 8;
            level++;
        }
        double arg = ((offset * interval) / (3 * perimeter)) * 2 * Math.PI + Math.PI / 4 + this.arg;
        double x = radius * Math.cos(arg);
        double y = radius * Math.sin(arg);
        offset++;

        Point2DWrapper point2D = new Point2DWrapper(x, y);
        point2D.setLevel(level);
        return point2D;
    }

    @Data
    private static class Point2DWrapper extends Point2D.Double {

        public Point2DWrapper(double x, double y) {
            super(x, y);
        }

        private int level;
    }
}
