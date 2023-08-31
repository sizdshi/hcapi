package com.hc.apiproject.model.vo;

import com.hc.hcapicommon.model.entity.InterfaceInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Kenneth shi
 * @Description:
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceInfoVO extends InterfaceInfo {

    private Integer totalNum;

    private static final long serialVersionUID = 1L;
}
