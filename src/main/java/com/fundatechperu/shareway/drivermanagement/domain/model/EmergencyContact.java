package com.fundatechperu.shareway.drivermanagement.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "dm", name = "emergency_contacts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmergencyContact {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 20)
  private String phone;

  @Column(nullable = false, length = 50)
  private String relationship;

  public EmergencyContact(String name, String phone, String relationship) {
    this.name = name.trim();
    this.phone = phone.trim();
    this.relationship = relationship.trim();
  }
}
