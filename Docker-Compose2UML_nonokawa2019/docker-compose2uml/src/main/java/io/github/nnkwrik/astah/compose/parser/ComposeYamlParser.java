package io.github.nnkwrik.astah.compose.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.nnkwrik.astah.constants.V3Configuration;
import io.github.nnkwrik.astah.model.Compose;
import io.github.nnkwrik.astah.model.Element;
import io.github.nnkwrik.astah.model.Network;
import io.github.nnkwrik.astah.model.Service;
import io.github.nnkwrik.astah.util.YamlUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * yamlテキストをComposeにパースする
 *
 * @author Reika Nonokawa
 */
public class ComposeYamlParser {

    private static final ObjectMapper jsonMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper yamlMapper = new YAMLMapper();

    /**
     * composeオブジェクトにパース
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static Compose parse(File file) throws IOException {
        JsonNode composeNode = jsonMapper.readTree(file);
        return parse(file.getName(), composeNode);
    }

    /**
     * composeオブジェクトにパース
     *
     * @param composeName
     * @param composeRaw
     * @return
     * @throws IOException
     */
    public static Compose parse(String composeName, String composeRaw) throws IOException {
        JsonNode composeNode = jsonMapper.readTree(composeRaw);
        return parse(composeName, composeNode);
    }

    /**
     * composeオブジェクトにパース
     *
     * @param composeName
     * @param composeNode
     * @return
     * @throws IOException
     */
    public static Compose parse(String composeName, JsonNode composeNode) {
        Compose compose = new Compose(composeName);
        String raw = YamlUtil.getRawText(composeNode);
        compose.setRaw(raw);

        //versionをパース
        JsonNode versionNode = composeNode.findValue(V3Configuration.VERSION);
        compose.setVersion(getText(versionNode));

        //サービスをパース
        JsonNode servicesNode = composeNode.findValue(V3Configuration.SERVICES);
        List<Service> services = parseServices(servicesNode);
        compose.setServices(services);

        //ネットワークをパース
        List<JsonNode> networksNodeList = composeNode.findValues(V3Configuration.NETWORKS);
        List<Network> networks = null;
        if (networksNodeList.size() > 0) {
            JsonNode networksNode = networksNodeList.get(networksNodeList.size() - 1);
            networks = parseNetworks(networksNode);
        } else {
            networks = new ArrayList<>();
        }
        networks.add(Network.defaultNetwork);
        compose.setNetworks(networks);

        //サービス間の関係を設置する
        completeServiceRelation(services, networks);

        //詳細情報を設置する
        LinkedHashMap<String, String> detailInfo = getInfoMap(composeNode);
        compose.setDetailInfo(detailInfo);

        return compose;
    }


    /**
     * servicesをパース
     *
     * @param servicesNode
     * @return
     * @throws JsonProcessingException
     */
    private static List<Service> parseServices(JsonNode servicesNode) {
        if (servicesNode == null) return null;

        List<Service> services = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> nodeIt = servicesNode.fields();
        while (nodeIt.hasNext()) {
            Map.Entry<String, JsonNode> serviceEntry = nodeIt.next();
            Service service = parseService(serviceEntry.getKey(), serviceEntry.getValue());
            String raw = YamlUtil.getRawText(serviceEntry);
            service.setRaw(raw);
            services.add(service);
        }
        return services;
    }

    /**
     * サービスをパース
     *
     * @param serviceName
     * @param serviceNode
     * @return
     */
    private static Service parseService(String serviceName, JsonNode serviceNode) {
        Service service = new Service(serviceName);

        //ポートをパース
        JsonNode portsNode = serviceNode.findValue(V3Configuration.SERVICES_PORTS);
        List<Element> ports = parsePorts(portsNode);
        service.setPorts(ports);

        //ビルドをパース
        JsonNode buildNode = serviceNode.findValue(V3Configuration.SERVICES_BUILD);
        Element build = parseBuild(buildNode);
        service.setBuild(build);

        //イメージをパース
        JsonNode imageNode = serviceNode.findValue(V3Configuration.SERVICES_IMAGE);
        Element image = parseImage(imageNode);
        service.setImage(image);

        //depends_onをパース
        JsonNode dependsonNode = serviceNode.findValue(V3Configuration.SERVICES_DEPENDSON);
        List<String> dependson = parseNameList(dependsonNode);
        service.setDependsOn(dependson);

        //linksをパース
        JsonNode linksNode = serviceNode.findValue(V3Configuration.SERVICES_LINKS);
        List<String> links = parseNameList(linksNode);
        //linksをdepends_onに加える
        if (links != null && links.size() > 0) {
            if (dependson == null || dependson.size() <= 0) {
                service.setDependsOn(links);
            } else {
                List<String> linkNames = dependson.stream()
                        .filter(l -> !dependson.contains(l)).collect(Collectors.toList());
                dependson.addAll(linkNames);
            }
        }

        //networksをパース
        JsonNode networksNode = serviceNode.findValue(V3Configuration.SERVICES_NETWORKS);
        List<String> networks = parseNameList(networksNode);
        service.setNetworks(networks);

        //detailInfoをパースし設置する
        LinkedHashMap<String, String> detailInfo = getInfoMap(serviceNode);
        service.setDetailInfo(detailInfo);

        return service;
    }

    /**
     * portsをパース
     *
     * @param portsNode
     * @return
     */
    private static List<Element> parsePorts(JsonNode portsNode) {
        if (portsNode == null) return null;

        //ports must is an array
        List<Element> ports = new ArrayList<>();
        for (final JsonNode portNode : portsNode) {
            Element port = new Element();
            if (portNode.isValueNode()) {
                //short syntax
                port.setName(portNode.asText());
            } else {
                //long syntax
                StringBuilder sb = new StringBuilder();

                JsonNode host = portNode.findValue(V3Configuration.SERVICES_PORTS_PUBLISHED);
                if (host != null) {
                    sb.append(host.asText() + ":");
                }

                JsonNode container = portNode.findValue(V3Configuration.SERVICES_PORTS_TARGET);
                sb.append(container.asText());

                JsonNode protocol = portNode.findValue(V3Configuration.SERVICES_PORTS_PROTOCOL);
                if (protocol != null && protocol.asText().equals("udp")) {
                    sb.append("/" + protocol.asText());
                }

                port.setName(sb.toString());
                port.setDetailInfo(getInfoMap(portNode));
            }
            ports.add(port);
        }
        return ports;
    }

    /**
     * buildをパース
     *
     * @param buildNode
     * @return
     */
    private static Element parseBuild(JsonNode buildNode) {
        if (buildNode == null) return null;

        Element build = new Element();

        if (buildNode.isValueNode()) {
            build.setName(buildNode.asText());
        } else {
            //full syntax
            JsonNode context = buildNode.findValue(V3Configuration.SERVICES_BUILD_CONTEXT);
            build.setName(context.asText());
            build.setDetailInfo(getInfoMap(buildNode));
        }
        return build;
    }

    /**
     * imageをパース
     *
     * @param node
     * @return
     */
    private static Element parseImage(JsonNode node) {
        if (node == null) return null;
        Element image = new Element();
        image.setName(getText(node));
        return image;
    }

    /**
     * ネットワークをパース
     *
     * @param networksNode
     * @return
     */
    private static List<Network> parseNetworks(JsonNode networksNode) {
        if (networksNode == null) return null;

        List<Network> networks = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> nodeIt = networksNode.fields();
        while (nodeIt.hasNext()) {
            Map.Entry<String, JsonNode> entry = nodeIt.next();
            Network network = new Network(entry.getKey());
            LinkedHashMap<String, String> detailInfo = getInfoMap(entry.getValue());
            network.setDetailInfo(detailInfo);
            networks.add(network);
        }
        return networks;
    }

    /**
     * node下のすべての項目をJson形式のmapでリターン
     *
     * @param node
     * @return
     */
    private static LinkedHashMap<String, String> getInfoMap(JsonNode node) {
        LinkedHashMap<String, String> otherInfo = new LinkedHashMap<>();
        Iterator<Map.Entry<String, JsonNode>> nodeIt = node.fields();
        while (nodeIt.hasNext()) {
            Map.Entry<String, JsonNode> entry = nodeIt.next();
            otherInfo.put(entry.getKey(), getText(entry.getValue()));
        }
        return otherInfo;
    }


    /**
     * Serviceオブジェクトに該当のnetworksオブジェクトを設置する
     *
     * @param services
     * @param networks
     */
    private static void completeServiceRelation(List<Service> services, List<Network> networks) {
        if (services == null || services.size() <= 0) {
            return;
        }
        for (final Service service : services) {
            List<String> dependsNames = service.getDependsOn();
            if (dependsNames == null || dependsNames.size() < 1) continue;
            for (final String dependsName : dependsNames) {
                Service dependsService = services.stream()
                        .filter(s -> s.getName().equals(dependsName))
                        .findFirst()
                        .get();

                //find shared networks
                List<String> networksName = new ArrayList<>();
                if (service.getNetworks() != null) {
                    networksName.addAll(service.getNetworks());
                }
                if (dependsService.getNetworks() != null) {
                    networksName.retainAll(dependsService.getNetworks());
                } else {
                    networks.clear();
                }


                List<Network> dependsNetwork = null;
                if (networks != null) {
                    dependsNetwork = networks.stream()
                            .filter(n -> networksName.contains(n.getName()))
                            .collect(Collectors.toList());
                } else {
                    dependsNetwork = new ArrayList<>();
                }
                if (dependsNetwork.isEmpty()) {
                    dependsNetwork.add(Network.defaultNetwork);
                }

                service.addDependsOrLinksService(dependsService, dependsNetwork);

            }

        }
    }


    private static List<String> parseNameList(JsonNode node) {
        if (node == null) return null;

        List<String> stringList;
        if (node.isArray()) {
            stringList = StreamSupport.stream(node.spliterator(), false)
                    .map(ComposeYamlParser::getText)
                    .collect(Collectors.toList());
        } else {
            stringList = new ArrayList<>();
            node.fieldNames().forEachRemaining(stringList::add);
        }
        return stringList;
    }


    public static String getText(JsonNode node) {
        if (node.isValueNode()) {
            return node.asText();
        } else {
            return node.toString();
        }
    }
}
