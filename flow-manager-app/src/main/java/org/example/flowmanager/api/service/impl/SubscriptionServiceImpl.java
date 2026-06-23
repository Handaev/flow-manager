package org.example.flowmanager.api.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.flowmanager.api.dto.ConversionMultipartFile;
import org.example.flowmanager.api.exception.ManagerException;
import org.example.flowmanager.api.security.context.UserContext;
import org.example.flowmanager.api.service.SubscriptionService;
import org.example.flowmanager.api.utils.Utils;
import org.example.subscription.client.dto.UserResponseDto;
import org.example.subscription.client.dto.UserResponseDto.SubscriptionTypeEnum;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserContext userContext;
    private final UserServiceImpl userServiceImpl;

    private static final Map<SubscriptionTypeEnum, Double> SUBSCRIPTION_LIMITS = Map.of(
            SubscriptionTypeEnum.FREE, Utils.MAX_SIZE_AVAILABLE_FILE_IN_MB
    );

    @Override
    public void checkingSubscriptionCurrentUser(ConversionMultipartFile conversionMultipartFile) throws ManagerException {
        String currentLoginUser = userContext.getLogin();

        UserResponseDto userResponseDto = userServiceImpl.getOrLoadUserSubscription(currentLoginUser);

        SubscriptionTypeEnum subscriptionType = userResponseDto.getSubscriptionType();
        if (Objects.isNull(subscriptionType)) {
            throw new ManagerException(String.format("Subscription type not found for user: %s", currentLoginUser));
        }

        if (!SubscriptionTypeEnum.PAID.equals(subscriptionType)) {
            checkingSubscriptionAndFileMatch(subscriptionType, conversionMultipartFile);
        }
    }

    private void checkingSubscriptionAndFileMatch(SubscriptionTypeEnum subscriptionType,
                                                  ConversionMultipartFile conversionMultipartFile)
    {
        long sizeFile = conversionMultipartFile.getSize();

        Double maxAllowedSize = SUBSCRIPTION_LIMITS.get(subscriptionType);
        if (Objects.isNull(maxAllowedSize)) {
            throw new ManagerException(String.format("Лимиты для типа подписки %s не настроены", subscriptionType));
        }

        double fileSizeInMb = (double) sizeFile / Utils.BYTES_IN_MB;

        if (fileSizeInMb > maxAllowedSize) {
            throw new ManagerException(String.format(
                    "Размер файла (%.2f MB) превышает лимит вашей подписки %s (макс. %.2f MB)",
                    fileSizeInMb, subscriptionType, maxAllowedSize
            ));
        }
    }
}
