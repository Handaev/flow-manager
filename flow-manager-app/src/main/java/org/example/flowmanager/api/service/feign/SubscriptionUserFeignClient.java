package org.example.flowmanager.api.service.feign;

import org.example.subscription.client.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "${services.subscription.name}", url = "${services.subscription.client.api.url}")
public interface SubscriptionUserFeignClient extends UserApi {
}
