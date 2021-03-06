package org.openlmis.stockmanagement.web;

import org.junit.Test;
import org.mockito.Mockito;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.StockEventProcessor;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_NO_FOLLOWING_PERMISSION;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_STOCK_EVENT_REASON_NOT_MATCH;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_EVENT_CREATE;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createStockEventDto;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StockEventsControllerTest extends BaseWebTest {

  private static final String CREATE_STOCK_EVENT_API = "/api/stockEvents";

  @MockBean
  private StockEventProcessor stockEventProcessor;

  @Test
  public void should_return_201_when_event_successfully_created() throws Exception {
    //given
    UUID uuid = UUID.randomUUID();
    when(stockEventProcessor.process(any(StockEventDto.class))).thenReturn(uuid);

    //when
    ResultActions resultActions = mvc.perform(post(CREATE_STOCK_EVENT_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(createStockEventDto())));

    //then
    resultActions.andDo(MockMvcResultHandlers.print())
        .andExpect(status().isCreated())
        .andExpect(content().string("\"" + uuid.toString() + "\""));
  }

  @Test
  public void should_return_403_when_user_has_not_permission() throws Exception {
    //given
    Mockito.doThrow(new PermissionMessageException(
        new Message(ERROR_NO_FOLLOWING_PERMISSION, STOCK_EVENT_CREATE)))
        .when(stockEventProcessor)
        .process(any());

    //when
    ResultActions resultActions = mvc.perform(post(CREATE_STOCK_EVENT_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(new StockEventDto())));

    //then
    resultActions.andExpect(status().isForbidden());
  }

  @Test
  public void should_return_400_when_validation_fails() throws Exception {
    //given
    Mockito.doThrow(new ValidationMessageException(new Message(ERROR_STOCK_EVENT_REASON_NOT_MATCH)))
        .when(stockEventProcessor)
        .process(any());

    //when
    ResultActions resultActions = mvc.perform(post(CREATE_STOCK_EVENT_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(new StockEventDto())));

    //then
    resultActions.andExpect(status().isBadRequest());
  }
}