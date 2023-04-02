package com.ss.abtest.service.impl;

import com.ss.abtest.exception.IllegalParamException;
import com.ss.abtest.mapper.FlightMapper;
import com.ss.abtest.mapper.LayerMapper;
import com.ss.abtest.pojo.domain.Flight;
import com.ss.abtest.pojo.domain.Version;
import com.ss.abtest.pojo.dto.FlightDto;
import com.ss.abtest.pojo.vo.Bucket;
import com.ss.abtest.pojo.vo.FlightUser;
import com.ss.abtest.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class FlightServiceImpl implements FlightService {

    @Autowired
    FlightMapper flightMapper;

    @Autowired
    LayerMapper layerMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FlightDto createFlight(FlightDto flightDto) {
        //1、参数校验。
        verifyFlightParams(flightDto);
        //2、数据库中创建实验
        addFlight(flightDto);
        //3、关联实验用户。
        addFlightUser(flightDto);
        //4、数据库中创建实验组
        addFlightVersions(flightDto);
        //5、分配流量
        distributeTraffic(flightDto);

        return flightDto;
    }

    /**
     * 关联实验用户。
     *
     * @param flightDto flightDto
     */
    private void addFlightUser(FlightDto flightDto) {
        List<FlightUser> list = flightDto.getFlightUsers();
        list.forEach(user -> flightMapper.addFlightUser(user));
    }

    /**
     * 分配流量
     *
     * @param flightDto flightDto
     */
    private void distributeTraffic(FlightDto flightDto) {
        // 1、检验实验层剩余流量
        checkLayerTraffic(flightDto);
        // 2、获取可分配流量桶 bucket
        List<Bucket> buckets = getDistributiveBucket(flightDto);
        // 3、数据库中创建数据
        buckets.forEach(bucket -> flightMapper.addFlightTraffic(bucket));
        // 4、更新实验层的剩余流量 LayerTraffic = LayerTraffic - FlightTraffic;
        layerMapper.updateLayerTraffic(flightDto.getFlightTraffic());
    }

    /**
     * 获取可分配流量桶
     *
     * @param flightDto flightDto
     * @return List<Bucket>
     */
    private List<Bucket> getDistributiveBucket(FlightDto flightDto) {
        Long flightId = flightDto.getFlightId();
        Long layerId = flightDto.getLayerId();
        int flightTraffic = flightDto.getFlightTraffic();
        List<Integer> list = flightMapper.getLayerTraffic(layerId);
        list.sort(Comparator.comparingInt(o -> o));
        List<Bucket> buckets = new ArrayList<>();
        int index = 0;
        int sum = 0;
        for (int i = 0; i < 1000 && sum <= flightTraffic; i++) {
            if (index < list.size() && list.get(index) == i) {
                index++;
            } else {
                Bucket bucket = new Bucket(flightId, layerId, i);
                buckets.add(bucket);
                sum++;
            }
        }
        return buckets;
    }

    private void checkLayerTraffic(FlightDto flightDto) {
        int traffic = layerMapper.getTraffic(flightDto.getLayerId());
        if (traffic < flightDto.getFlightTraffic()) {
            throw new IllegalParamException("layer traffic (" + traffic + ") < flight traffic" + flightDto.getFlightTraffic() + " error");
        }
    }

    /**
     * 添加 实验组
     *
     * @param flightDto flightDto
     */
    private void addFlightVersions(FlightDto flightDto) {
        List<Version> versions = flightDto.getVersionEntry();
        for (Version version : versions) {
            Version v1 = flightMapper.addVersion(version);
            version.setVersionId(v1.getVersionId());
        }
    }

    /**
     * 添加实验
     *
     * @param flightDto flightDto
     */
    private void addFlight(FlightDto flightDto) {
        Flight flight = flightMapper.addFlight(flightDto.getFlightEntry());
        flightDto.setFlightId(flight.getFlightId());
    }

    /**
     * 检验参数
     *
     * @param flightDto flightDto
     */
    private void verifyFlightParams(FlightDto flightDto) {
        if (flightDto == null) {
            throw new IllegalParamException("flightDto is null");
        }
        flightDto.verifyParams();
    }
}
