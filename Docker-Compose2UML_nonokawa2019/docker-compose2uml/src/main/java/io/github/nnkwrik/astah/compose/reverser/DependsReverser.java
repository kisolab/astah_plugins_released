package io.github.nnkwrik.astah.compose.reverser;

import com.change_vision.jude.api.inf.editor.CompositeStructureDiagramEditor;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IElement;
import io.github.nnkwrik.astah.astah.builder.DependsOrLinksBuilder;
import io.github.nnkwrik.astah.model.Network;
import io.github.nnkwrik.astah.model.Service;
import io.github.nnkwrik.astah.util.ColorUtil;
import io.github.nnkwrik.astah.util.YamlUtil;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * depends on、links、ネットワークのリバーサー
 *
 * @author Reika Nonokawa
 */
public class DependsReverser {

    private CompositeStructureDiagramEditor csDiagramEditor;

    public DependsReverser(CompositeStructureDiagramEditor csDiagramEditor) {
        this.csDiagramEditor = csDiagramEditor;
    }

    /**
     * depends on/linksをリバース
     *
     * @param serviceElements
     */

    public void reverse(Map<Service, List<IElement>> serviceElements) throws InvalidEditingException, InvalidUsingException {
        reverse(serviceElements,null);
    }

    public void reverse(Map<Service, List<IElement>> serviceElements,Map<Service, Point2D> serviceLocation) throws InvalidEditingException, InvalidUsingException {
        //networkが使われている回数をカウント
        Map<Network, Long> networkUsageCount = NetworksHelper.networkUsageCount(serviceElements.keySet());
        Map<Network, String> networkColorMap = new HashMap<>();

        for (Service service : serviceElements.keySet()) {
            //対象serviceと対応のiclass
            IClass from = (IClass) serviceElements.get(service).get(0);
            for (Map.Entry<Service, List<Network>> entry : service.getDependsOrLinksService().entrySet()) {
                //対象serviceが依存しているserviceのiclass
                IClass to = (IClass) serviceElements.get(entry.getKey()).get(0);
                List<Network> networks = entry.getValue();

                DependsOrLinksBuilder.getBuilder(csDiagramEditor, from, to)
                        .name(getName(networks))
                        .color(getColor(networks, networkUsageCount, networkColorMap))
                        .taggedValues(getTaggedValues(networks))
                        .build();
            }
        }
        resetColor();

        //扇円の配置位置にする
        if (serviceLocation == null){
            new LocationRegulator(serviceElements, networkUsageCount).adjust();
        }else {
            new LocationRegulator(serviceElements, networkUsageCount).adjust(serviceLocation);
        }

    }


    /**
     * 使用networksをつなげたStringをnameとする
     *
     * @param networks
     * @return
     */
    private String getName(List<Network> networks) {
        String dependencyName;
        if (networks.size() > 1) {
            StringBuilder sb = new StringBuilder();
            networks.stream()
                    .filter(n -> n != Network.defaultNetwork)
                    .map(n -> n.getName()).forEach(n -> {
                sb.append(',');
                sb.append(n);
            });
            sb.deleteCharAt(0);//remove "," in head
            dependencyName = sb.toString();
        } else if (networks.get(0) == Network.defaultNetwork) {
            dependencyName = "";
        } else {
            dependencyName = networks.get(0).getName();
        }
        return dependencyName;
    }

    /**
     * networkの頻度に対応色をcolorとする
     *
     * @param networks
     * @param networkUsageCount
     * @return
     */
    private String getColor(List<Network> networks, Map<Network, Long> networkUsageCount, Map<Network, String> networkColorMap) {
        if (networks.size() == 1 && networks.get(0) == Network.defaultNetwork) {
            return "#000000";
        }
        //最も使われているネットワーク,デフォルトネットワークを含む
        Network mainNetwork = NetworksHelper.getMainNetwork(networks, networkUsageCount);

        String color = networkColorMap.get(mainNetwork);
        if (color == null) {
            color = ColorUtil.nextColor();
            networkColorMap.put(mainNetwork, color);
        }

        return color;
    }

    private void resetColor() {
        ColorUtil.resetColor();
    }

    private Map<String, String> getTaggedValues(List<Network> networks) {
        Map<String, String> taggedValue = new LinkedHashMap<>();
        for (Network network : networks) {
            if (network == Network.defaultNetwork) continue;
            Map<String, String> detailInfo = network.getDetailInfo();
            if (detailInfo == null || detailInfo.size() <= 0) {
                taggedValue.put(network.getName(), "");
            } else {
                taggedValue.put(network.getName(), YamlUtil.mapToJson(detailInfo));
            }
        }
        return taggedValue;
    }

}
