declare namespace API {
  type BaseResponseboolean = {
    code?: number;
    data?: boolean;
    message?: string;
  };

  type BaseResponseInterfaceInfoVO = {
    code?: number;
    data?: InterfaceInfoVO;
    message?: string;
  };

  type BaseResponseListInterfaceInfo = {
    code?: number;
    data?: InterfaceInfo[];
    message?: string;
  };

  type BaseResponseListInterfaceInfoVO = {
    code?: number;
    data?: InterfaceInfoVO[];
    message?: string;
  };

  type BaseResponseListUserInterfaceInfo = {
    code?: number;
    data?: UserInterfaceInfo[];
    message?: string;
  };

  type BaseResponseLoginUserVO = {
    code?: number;
    data?: LoginUserVO;
    message?: string;
  };

  type BaseResponselong = {
    code?: number;
    data?: number;
    message?: string;
  };

  type BaseResponseobject = {
    code?: number;
    data?: Record<string, any>;
    message?: string;
  };

  type BaseResponsePageInterfaceInfo = {
    code?: number;
    data?: PageInterfaceInfo;
    message?: string;
  };

  type BaseResponsePageInterfaceInfoVO = {
    code?: number;
    data?: PageInterfaceInfoVO;
    message?: string;
  };

  type BaseResponsePageUserInterfaceInfo = {
    code?: number;
    data?: PageUserInterfaceInfo;
    message?: string;
  };

  type BaseResponsestring = {
    code?: number;
    data?: string;
    message?: string;
  };

  type BaseResponseUser = {
    code?: number;
    data?: User;
    message?: string;
  };

  type BaseResponseUserInterfaceInfo = {
    code?: number;
    data?: UserInterfaceInfo;
    message?: string;
  };

  type BaseResponseUserVO = {
    code?: number;
    data?: UserVO;
    message?: string;
  };

  type DeleteRequest = {
    id?: number;
  };

  type getInterfaceInfoVOByIdUsingGETParams = {
    /** id */
    id?: number;
  };

  type getUserByIdUsingGETParams = {
    /** id */
    id?: number;
  };

  type getUserInterfaceInfoByIdUsingGETParams = {
    /** id */
    id?: number;
  };

  type getUserVOByIdUsingGETParams = {
    /** id */
    id?: number;
  };

  type IdRequest = {
    id?: number;
  };

  type InterfaceInfo = {
    createTime?: string;
    description?: string;
    host?: string;
    id?: number;
    isDelete?: number;
    method?: string;
    name?: string;
    requestHeader?: string;
    requestParams?: string;
    requestParamsRemark?: string;
    responseHeader?: string;
    responseParamsRemark?: string;
    status?: number;
    updateTime?: string;
    url?: string;
    userId?: number;
  };

  type InterfaceInfoAddRequest = {
    description?: string;
    host?: string;
    method?: string;
    name?: string;
    requestHeader?: string;
    requestParams?: string;
    requestParamsRemark?: RequestParamsRemarkVO[];
    responseHeader?: string;
    responseParamsRemark?: ResponseParamsRemarkVO[];
    status?: number;
    url?: string;
  };

  type InterfaceInfoInvokeRequest = {
    host?: string;
    id?: number;
    method?: string;
    requestParams?: string;
  };

  type InterfaceInfoQueryRequest = {
    createTime?: string;
    current?: number;
    description?: string;
    host?: string;
    id?: number;
    isDelete?: number;
    method?: string;
    name?: string;
    pageSize?: number;
    requestHeader?: string;
    requestParamsRemark?: RequestParamsRemarkVO[];
    responseHeader?: string;
    responseParamsRemark?: ResponseParamsRemarkVO[];
    searchText?: string;
    sortField?: string;
    sortOrder?: string;
    status?: number;
    url?: string;
    userId?: number;
  };

  type InterfaceInfoUpdateRequest = {
    description?: string;
    host?: string;
    id?: number;
    method?: string;
    name?: string;
    requestHeader?: string;
    requestParams?: string;
    requestParamsRemark?: RequestParamsRemarkVO[];
    responseHeader?: string;
    responseParamsRemark?: ResponseParamsRemarkVO[];
    status?: number;
    url?: string;
  };

  type InterfaceInfoVO = {
    createTime?: string;
    description?: string;
    host?: string;
    id?: number;
    isOwnerByCurrentUser?: boolean;
    leftNum?: number;
    method?: string;
    name?: string;
    requestHeader?: string;
    requestParams?: string;
    requestParamsRemark?: RequestParamsRemarkVO[];
    responseHeader?: string;
    responseParamsRemark?: ResponseParamsRemarkVO[];
    status?: number;
    totalNum?: number;
    updateTime?: string;
    url?: string;
    user?: UserVO;
    userId?: number;
  };

  type listInterfaceInfoByPageUsingGETParams = {
    createTime?: string;
    current?: number;
    description?: string;
    host?: string;
    id?: number;
    isDelete?: number;
    method?: string;
    name?: string;
    pageSize?: number;
    requestHeader?: string;
    'requestParamsRemark[0].id'?: number;
    'requestParamsRemark[0].isRequired'?: string;
    'requestParamsRemark[0].name'?: string;
    'requestParamsRemark[0].remark'?: string;
    'requestParamsRemark[0].type'?: string;
    responseHeader?: string;
    'responseParamsRemark[0].id'?: number;
    'responseParamsRemark[0].name'?: string;
    'responseParamsRemark[0].remark'?: string;
    'responseParamsRemark[0].type'?: string;
    searchText?: string;
    sortField?: string;
    sortOrder?: string;
    status?: number;
    url?: string;
    userId?: number;
  };

  type listInterfaceInfoUsingPOSTParams = {
    createTime?: string;
    current?: number;
    description?: string;
    host?: string;
    id?: number;
    isDelete?: number;
    method?: string;
    name?: string;
    pageSize?: number;
    requestHeader?: string;
    'requestParamsRemark[0].id'?: number;
    'requestParamsRemark[0].isRequired'?: string;
    'requestParamsRemark[0].name'?: string;
    'requestParamsRemark[0].remark'?: string;
    'requestParamsRemark[0].type'?: string;
    responseHeader?: string;
    'responseParamsRemark[0].id'?: number;
    'responseParamsRemark[0].name'?: string;
    'responseParamsRemark[0].remark'?: string;
    'responseParamsRemark[0].type'?: string;
    searchText?: string;
    sortField?: string;
    sortOrder?: string;
    status?: number;
    url?: string;
    userId?: number;
  };

  type listUserInterfaceInfoByPageUsingPOSTParams = {
    current?: number;
    id?: number;
    interfaceInfoId?: number;
    leftNum?: number;
    pageSize?: number;
    searchText?: string;
    sortField?: string;
    sortOrder?: string;
    status?: number;
    totalNum?: number;
    userId?: number;
  };

  type listUserInterfaceInfoUsingPOSTParams = {
    current?: number;
    id?: number;
    interfaceInfoId?: number;
    leftNum?: number;
    pageSize?: number;
    searchText?: string;
    sortField?: string;
    sortOrder?: string;
    status?: number;
    totalNum?: number;
    userId?: number;
  };

  type LoginUserVO = {
    createTime?: string;
    id?: number;
    updateTime?: string;
    userAvatar?: string;
    userName?: string;
    userProfile?: string;
    userRole?: string;
  };

  type OrderItem = {
    asc?: boolean;
    column?: string;
  };

  type PageInterfaceInfo = {
    countId?: string;
    current?: number;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: OrderItem[];
    pages?: number;
    records?: InterfaceInfo[];
    searchCount?: boolean;
    size?: number;
    total?: number;
  };

  type PageInterfaceInfoVO = {
    countId?: string;
    current?: number;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: OrderItem[];
    pages?: number;
    records?: InterfaceInfoVO[];
    searchCount?: boolean;
    size?: number;
    total?: number;
  };

  type PageUserInterfaceInfo = {
    countId?: string;
    current?: number;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: OrderItem[];
    pages?: number;
    records?: UserInterfaceInfo[];
    searchCount?: boolean;
    size?: number;
    total?: number;
  };

  type RequestParamsRemarkVO = {
    id?: number;
    isRequired?: string;
    name?: string;
    remark?: string;
    type?: string;
  };

  type ResponseParamsRemarkVO = {
    id?: number;
    name?: string;
    remark?: string;
    type?: string;
  };

  type uploadFileUsingPOSTParams = {
    biz?: string;
  };

  type User = {
    accessKey?: string;
    createTime?: string;
    id?: number;
    isDelete?: number;
    mpOpenId?: string;
    secretKey?: string;
    unionId?: string;
    updateTime?: string;
    userAccount?: string;
    userAvatar?: string;
    userName?: string;
    userPassword?: string;
    userProfile?: string;
    userRole?: string;
  };

  type UserAddRequest = {
    userAccount?: string;
    userAvatar?: string;
    userName?: string;
    userRole?: string;
  };

  type UserInterfaceInfo = {
    createTime?: string;
    id?: number;
    interfaceInfoId?: number;
    isDelete?: number;
    leftNum?: number;
    status?: number;
    totalNum?: number;
    updateTime?: string;
    userId?: number;
  };

  type UserInterfaceInfoAddRequest = {
    interfaceInfoId?: number;
    leftNum?: number;
    totalNum?: number;
    userId?: number;
  };

  type UserInterfaceInfoUpdateRequest = {
    id?: number;
    leftNum?: number;
    status?: number;
    totalNum?: number;
  };

  type UserLoginRequest = {
    userAccount?: string;
    userPassword?: string;
  };

  type UserRegisterRequest = {
    checkPassword?: string;
    userAccount?: string;
    userPassword?: string;
  };

  type UserUpdateMyRequest = {
    userAvatar?: string;
    userName?: string;
    userProfile?: string;
  };

  type UserUpdateRequest = {
    id?: number;
    userAvatar?: string;
    userName?: string;
    userProfile?: string;
    userRole?: string;
  };

  type UserVO = {
    accessKey?: string;
    createTime?: string;
    id?: number;
    secretKey?: string;
    userAccount?: string;
    userAvatar?: string;
    userName?: string;
    userProfile?: string;
    userRole?: string;
  };
}
