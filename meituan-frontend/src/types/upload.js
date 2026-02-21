/**
 * 批量上传全部商品 - 数据类型定义
 */

/**
 * 商品统计信息
 * @typedef {Object} ProductStats
 * @property {number} total - 商品总数
 * @property {number} pending - 待上传数量
 * @property {number} uploaded - 已上传数量
 * @property {number} failed - 上传失败数量
 */

/**
 * 商品预览信息
 * @typedef {Object} ProductPreview
 * @property {number} id - 商品ID
 * @property {string} productName - 商品名称
 * @property {string} categoryName - 类目名称
 * @property {number} price - 价格
 * @property {number} stock - 库存
 * @property {string} createdTime - 创建时间
 * @property {number} status - 状态 (0:待上传, 1:已上传, 2:失败)
 */

/**
 * 操作日志
 * @typedef {Object} OperationLog
 * @property {number} id - 日志ID
 * @property {string} operationType - 操作类型
 * @property {string} operationTime - 操作时间
 * @property {number} productCount - 商品数量
 * @property {string} status - 状态 (SUCCESS, FAILED)
 * @property {string} message - 消息
 */

/**
 * 页面状态
 * @typedef {Object} PageState
 * @property {boolean} loading - 加载中
 * @property {boolean} generating - 生成中
 * @property {ProductStats} stats - 统计信息
 * @property {ProductPreview[]} recentProducts - 最近商品
 * @property {OperationLog[]} recentLogs - 最近操作
 */

export default {}
