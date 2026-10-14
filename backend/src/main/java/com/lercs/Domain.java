package com.lercs;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

enum Role { CITIZEN, RESPONDER, COORDINATOR, ADMIN }
enum EmergencyStatus { REPORTED, UNDER_REVIEW, VERIFIED, ASSIGNED, IN_PROGRESS, RESOLVED, CANCELLED }
enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
enum Availability { AVAILABLE, BUSY, OFFLINE }
enum Verification { PENDING, VERIFIED, REJECTED }
enum ResourceStatus { AVAILABLE, LOW_STOCK, OUT_OF_STOCK, RESERVED, UNAVAILABLE }
enum AssignmentStatus { ASSIGNED, ACCEPTED, REJECTED, IN_PROGRESS, COMPLETED, IN_USE, RETURNED, CANCELLED }
enum FacilityStatus { OPEN, FULL, CLOSED }

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity @Table(name="users")
class User {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(nullable=false,unique=true) @Email String email;
    @JsonIgnore @Column(nullable=false) String password;
    @Column(nullable=false) String fullName;
    String phone;
    @Enumerated(EnumType.STRING) @Column(nullable=false) Role role=Role.CITIZEN;
    boolean active=true;
    Instant createdAt=Instant.now();
    protected User() {}
    User(String email,String password,String fullName,String phone,Role role){this.email=email;this.password=password;this.fullName=fullName;this.phone=phone;this.role=role;}
}

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity @Table(name="emergency_categories")
class EmergencyCategory {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(nullable=false,unique=true) String name;
    boolean active=true;
    protected EmergencyCategory() {}
    EmergencyCategory(String name){this.name=name;}
}

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity @Table(name="emergencies",indexes={@Index(columnList="status,priority"),@Index(columnList="createdAt")})
class Emergency {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @ManyToOne(optional=false) @JsonIgnoreProperties({"hibernateLazyInitializer","handler"}) User reporter;
    @ManyToOne(optional=false) @JsonIgnoreProperties({"hibernateLazyInitializer","handler"}) EmergencyCategory category;
    @Column(nullable=false) String title;
    @Column(nullable=false,length=3000) String description;
    @Enumerated(EnumType.STRING) Priority priority=Priority.MEDIUM;
    @Enumerated(EnumType.STRING) EmergencyStatus status=EmergencyStatus.REPORTED;
    @Min(0) int peopleAffected;
    @Column(nullable=false) String address;
    String city; String state; String postalCode;
    Double latitude; Double longitude;
    String assistance;
    String evidenceFileName;
    Instant createdAt=Instant.now();
    Instant updatedAt=Instant.now();
    protected Emergency() {}
}

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity @Table(name="emergency_status_history")
class StatusHistory {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @ManyToOne(optional=false) @JsonIgnoreProperties({"reporter","category","hibernateLazyInitializer","handler"}) Emergency emergency;
    @Enumerated(EnumType.STRING) EmergencyStatus oldStatus;
    @Enumerated(EnumType.STRING) EmergencyStatus newStatus;
    @ManyToOne(optional=false) @JsonIgnoreProperties({"hibernateLazyInitializer","handler"}) User changedBy;
    String comment; Instant changedAt=Instant.now();
    protected StatusHistory() {}
    StatusHistory(Emergency e,EmergencyStatus oldStatus,EmergencyStatus next,User actor,String comment){this.emergency=e;this.oldStatus=oldStatus;this.newStatus=next;this.changedBy=actor;this.comment=comment;}
}

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity @Table(name="responder_profiles")
class ResponderProfile {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @OneToOne(optional=false) @JsonIgnoreProperties({"hibernateLazyInitializer","handler"}) User user;
    @Column(length=1000) String skills;
    String experience;
    @Enumerated(EnumType.STRING) Availability availability=Availability.OFFLINE;
    @Enumerated(EnumType.STRING) Verification verification=Verification.PENDING;
    Double latitude; Double longitude; Instant registeredAt=Instant.now();
    protected ResponderProfile() {}
}

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity @Table(name="responder_assignments")
class ResponderAssignment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @ManyToOne(optional=false) @JsonIgnoreProperties({"hibernateLazyInitializer","handler"}) Emergency emergency;
    @ManyToOne(optional=false) @JsonIgnoreProperties({"hibernateLazyInitializer","handler"}) ResponderProfile responder;
    @ManyToOne(optional=false) @JsonIgnoreProperties({"hibernateLazyInitializer","handler"}) User assignedBy;
    @Enumerated(EnumType.STRING) AssignmentStatus status=AssignmentStatus.ASSIGNED;
    Instant assignedAt=Instant.now(); Instant acceptedAt; Instant completedAt; String notes;
    protected ResponderAssignment() {}
}

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity @Table(name="resources",indexes=@Index(columnList="category,status"))
class Resource {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(nullable=false) String name;
    @Column(nullable=false) String category;
    String description;
    @Min(0) int quantity;
    @Min(0) int availableQuantity;
    String unit; String location;
    @Enumerated(EnumType.STRING) ResourceStatus status=ResourceStatus.AVAILABLE;
    boolean active=true; Instant createdAt=Instant.now(); Instant updatedAt=Instant.now();
    protected Resource() {}
}

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity @Table(name="resource_assignments")
class ResourceAssignment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @ManyToOne(optional=false) @JsonIgnoreProperties({"hibernateLazyInitializer","handler"}) Emergency emergency;
    @ManyToOne(optional=false) @JsonIgnoreProperties({"hibernateLazyInitializer","handler"}) Resource resource;
    @ManyToOne(optional=false) @JsonIgnoreProperties({"hibernateLazyInitializer","handler"}) User assignedBy;
    @Min(1) int quantity;
    @Min(0) int returnedQuantity;
    @Enumerated(EnumType.STRING) AssignmentStatus status=AssignmentStatus.ASSIGNED;
    Instant assignedAt=Instant.now(); String notes;
    protected ResourceAssignment() {}
}

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity @Table(name="shelters")
class Shelter {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(nullable=false) String name;
    @Column(nullable=false) String address;
    String city; String state; String contactNumber;
    Double latitude; Double longitude;
    @Min(1) int capacity; @Min(0) int occupiedCapacity;
    String facilities; String accessibility;
    @Enumerated(EnumType.STRING) FacilityStatus status=FacilityStatus.OPEN;
    protected Shelter() {}
}

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity @Table(name="hospitals")
class Hospital {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(nullable=false) String name;
    @Column(nullable=false) String address;
    String city; String phone;
    Double latitude; Double longitude;
    boolean emergencyAvailable;
    @Min(0) int bedCapacity; @Min(0) int availableBeds;
    boolean ambulanceAvailable; String services;
    @Enumerated(EnumType.STRING) FacilityStatus status=FacilityStatus.OPEN;
    protected Hospital() {}
}

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity @Table(name="notifications",indexes=@Index(columnList="user_id,readAt"))
class Notification {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @ManyToOne(optional=false) @JsonIgnoreProperties({"hibernateLazyInitializer","handler"}) User user;
    @Column(nullable=false) String title;
    @Column(nullable=false,length=1500) String message;
    String type; Instant readAt; Instant createdAt=Instant.now();
    protected Notification() {}
    Notification(User user,String title,String message,String type){this.user=user;this.title=title;this.message=message;this.type=type;}
}

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity @Table(name="announcements")
class Announcement {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(nullable=false) String title;
    @Column(nullable=false,length=3000) String message;
    @Enumerated(EnumType.STRING) Priority priority=Priority.MEDIUM;
    String targetAudience="ALL";
    @ManyToOne(optional=false) @JsonIgnoreProperties({"hibernateLazyInitializer","handler"}) User createdBy;
    Instant createdAt=Instant.now(); Instant expiryAt; boolean active=true;
    protected Announcement() {}
}

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity @Table(name="audit_logs",indexes=@Index(columnList="createdAt"))
class AuditLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @ManyToOne @JsonIgnoreProperties({"hibernateLazyInitializer","handler"}) User user;
    String action; String entityType; String entityId;
    @Column(length=2000) String description; String ipAddress; Instant createdAt=Instant.now();
    protected AuditLog() {}
    AuditLog(User user,String action,String entityType,String entityId,String description){this.user=user;this.action=action;this.entityType=entityType;this.entityId=entityId;this.description=description;}
}
