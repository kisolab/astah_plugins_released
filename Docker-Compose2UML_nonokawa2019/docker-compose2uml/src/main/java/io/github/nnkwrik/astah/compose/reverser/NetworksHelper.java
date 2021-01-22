package io.github.nnkwrik.astah.compose.reverser;

import io.github.nnkwrik.astah.model.Network;
import io.github.nnkwrik.astah.model.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Reika Nonokawa
 */
public class NetworksHelper {

    /**
     * networkとnetworkが使われている回数をカウントする
     * @param servicesSet
     * @return
     */
    public static Map<Network, Long> networkUsageCount(Set<Service> servicesSet) {
        //key = network,value = すべてのサービスがこのネットワークにdependした回数
        return servicesSet.stream()
                .map(s -> s.getDependsOrLinksService().values())
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(n -> n, Collectors.counting()));
    }

    /**
     * サービスと関係ネットワークの対応
     * @param serviceSet
     * @param networkUsageCount
     * @return
     */
    public static Map<Service, List<Network>> relateNetwork(Set<Service> serviceSet, Map<Network, Long> networkUsageCount) {
        Map<Service, List<Network>> relateNetwork = new HashMap<>();
        for (Service service : serviceSet) {

            List<Network> networks = new ArrayList<>();
            relateNetwork.put(service, networks);

            //依存をするサービス
            service.getDependsOrLinksService().values().stream()
                    .flatMap(Collection::stream)
                    .forEach(e -> networks.add(e));

            //依存されるサービス
            serviceSet.stream()
                    .filter(s -> s.getDependsOrLinksService().keySet().contains(service))
                    .map(s -> s.getDependsOrLinksService().get(service))//find mianNetwork
                    .map(nList -> getMainNetwork(nList, networkUsageCount))
                    .forEach(e -> networks.add(e));

        }

        return relateNetwork;

    }

    /**
     * メインネットワーク(networksのなかでもっとも使われるネットワーク)を探す
     * @param networks
     * @param networkUsageCount
     * @return
     */
    public static Network getMainNetwork(List<Network> networks, Map<Network, Long> networkUsageCount) {
        Network mainNetwork;
        if (networks.size() > 1) {
            mainNetwork = networks.stream()
                    .filter(n -> n != Network.defaultNetwork)
                    .sorted((o1, o2) -> networkUsageCount.get(o2).compareTo(networkUsageCount.get(o1)))
                    .findFirst().get();
        } else {
            mainNetwork = networks.get(0);
        }
        return mainNetwork;
    }

}
