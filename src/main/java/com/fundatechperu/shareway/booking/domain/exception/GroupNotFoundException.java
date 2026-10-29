package com.fundatechperu.shareway.booking.domain.exception;

import com.fundatechperu.shareway.shared.exception.ResourceNotFoundException;
import java.util.UUID;

public class GroupNotFoundException extends ResourceNotFoundException {
  public GroupNotFoundException(UUID groupId) {
    super("Group not found: " + groupId);
  }
}
