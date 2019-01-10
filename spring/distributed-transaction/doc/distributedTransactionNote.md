# 分布式事务解决方案笔记

![distributedTransaction-problems](http://static.roncoo.com/images/yCGMB8jPtrsxKWf5PCQBpGCeKZKhzPBb.jpg)

电商购物支付流程中的分布式事务问题分析
1. 电商平台中创建订单：预留库存、预扣减积分、锁定优惠券，此时电商平台内各服务间会有分布式事务问题，因为此时已经要跨多个内部服务修改数据；
2. 支付平台中创建支付订单（选银行卡支付）：查询账户、查询限制规则，符合条件的就创建支付订单并跳转银行，此时不会有分布式事务问题，因为还不会跨服务改数据；
3. 银行平台中创建交易订单：查找账户、创建交易记录、判断账户余额并扣款、增加积分、通知支付平台，此时也会有分布式事务问题（如果是服务化架构的话）；
4. 支付平台收到银行扣款结果：更改订单状态、给账户加款、给积分帐户增加积分、生成会计分录、通知电商平台等，此时也会有分布式事务问题；
5. 电商平台收到支付平台的支付结果：更改订单状态、扣减库存、扣减积分、使用优惠券、增加消费积分等，系统内部各服务间调用也会遇到分布式事问题。

![distributedTransaction-process](http://static.roncoo.com/images/K4QcRFdKnbkT44AFFtzQYCAAX25FifDS.jpg)

支付平台收到银行扣款结果后的内部处理流程：
1. 支付平台的支付网关对银行通知结果进行校验，然后调用支付订单服务执行支付订单处理；
2. 支付订单服务根据银行扣款结果更改支付订单状态；
3. 调用资金账户服务给电商平台的商户账户加款（实际过程中可能还会有各种的成本计费；如果是余额支付，还可能是同时从用户账户扣款，给商户账户加款）；
4. 调用积分服务给用户积分账户增加积分；
5. 调用会计服务向会计（财务）系统写进交易原始凭证生成会计分录；
6. 调用通知服务将支付处理结果通知电商平台。

分布式事务问题的代码场景
```
/** 支付订单处理**/
@Transactional(rollbackFor = Exception.class)
public void completeOrder() {
	orderDao.update(); // 订单服务本地更新订单状态
	accountService.update(); // 调用资金账户服务给资金帐户加款
	pointService.update(); // 调用积分服务给积分帐户增加积分
	accountingService.insert(); // 调用会计服务向会计系统写入会计原始凭证
	merchantNotifyService.notify(); // 调用商户通知服务向商户发送支付结果通知
}
```

## 可靠消息的最终一致性方案
异步确保型（可靠消息最终一致）
- 对应支付系统会计异步记账业务
- 银行通知结果信息存储与驱动订单处理
- 可以异步，但数据绝对不能丢，而且一定要记账成功

![distributedTransactionFinal-min](http://www.wailian.work/images/2019/01/08/distributedTransactionFinal-min.png)

## 最大努力通知型方案
最大努力通知型
- 对应支付系统的商户通知业务场景
- 按规律进行通知，不保证数据一定能通知成功，但会提供可查询操作接口进行核对

![distributedTransactionMax-min](http://www.wailian.work/images/2019/01/08/distributedTransactionMax-min.png)

## TCC两阶段型方案
TCC（两阶段型、补偿型）
- 对应支付系统的订单账户操作：订单处理、资金账户处理、积分账户处理
- 实时性要求比较高，数据必须可靠

![distributedTransactionTCC-min](http://www.wailian.work/images/2019/01/08/distributedTransactionTCC-min.png)

## 实战应用场景
在支付系统中的实战应用场景

![distributedTransactionInAction-min](http://www.wailian.work/images/2019/01/08/distributedTransactionInAction-min.png)

可靠消息服务方案的特点：
1. 可独立部署、独立伸缩（扩展性）；
1. 兼容所有实现JMS标准的MQ中间件；
1. 能降低业务系统与消息系统间的耦合性；
1. 可实现数据可靠的前提下确保最终一致。

TCC方案的特点：
1. 不与具体的服务框架耦合（在RPC框架中通用）；
1. 位于业务服务层，而非资源层；
1. 可以灵活选择业务资源的锁定粒度；
1. 适用于强隔离性、严格一致性要求的业务场景；
1. 适用于执行时间较短的业务。

## References
- [微服务架构的分布式事务解决方案](https://www.roncoo.com/view/20)