package com.example.workshop06.api;

import com.example.workshop06.model.ActivityLogResponse;
import com.example.workshop06.model.AddOnRequest;
import com.example.workshop06.model.AddOnResponse;
import com.example.workshop06.model.AddressRequest;
import com.example.workshop06.model.AddressResponse;
import com.example.workshop06.model.CurrentPlanItemResponse;
import com.example.workshop06.model.CurrentPlanResponse;
import com.example.workshop06.model.EmployeeDashboardResponse;
import com.example.workshop06.model.LocationRequest;
import com.example.workshop06.model.LocationResponse;
import com.example.workshop06.model.LoginRequest;
import com.example.workshop06.model.LoginResponse;
import com.example.workshop06.model.MeResponse;
import com.example.workshop06.model.MyAddonResponse;
import com.example.workshop06.model.ToggleActiveRequest;
import com.example.workshop06.model.UpdateProfileRequest;
import com.example.workshop06.model.*;
import com.example.workshop06.model.ServiceRequestResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/me")
    Call<MeResponse> getMe(@Header("Authorization") String token);

    @PUT("api/me/profile")
    Call<Void> updateProfile(
            @Header("Authorization") String token,
            @Body UpdateProfileRequest body
    );

    @GET("api/me/current-plan")
    Call<CurrentPlanResponse> getCurrentPlan(@Header("Authorization") String token);

    @GET("api/billing/address")
    Call<AddressResponse> getBillingAddress(@Header("Authorization") String token);

    @POST("api/billing/address")
    Call<Void> createBillingAddress(
            @Header("Authorization") String token,
            @Body AddressRequest body
    );

    @PUT("api/billing/address")
    Call<Void> updateBillingAddress(
            @Header("Authorization") String token,
            @Body AddressRequest body
    );

    @GET("api/me/plans")
    Call<List<CurrentPlanItemResponse>> getMyPlans(@Header("Authorization") String token);

    @GET("api/me/addons")
    Call<List<MyAddonResponse>> getMyAddOns(@Header("Authorization") String token);

    // this is to get the employeedashboard
    @GET("/api/employee/dashboard")
    Call<EmployeeDashboardResponse> getEmployeeDashboard();

    //summary for the counts
    @GET("/api/manager/reports/summary")
    Call<ManagerSummaryResponse> getManagerSummary();

    //endpoints for location
    @GET("/api/manager/location")
    Call<List<LocationResponse>> getLocations();

    @GET("/api/manager/location/{id}")
    Call<LocationResponse> getLocationById(@Path("id") int id);

    @POST("/api/manager/location")
    Call<LocationResponse> createLocation(@Body LocationRequest request);

    @PUT("/api/manager/location/{id}")
    Call<LocationResponse> updateLocation(@Path("id") int id, @Body LocationRequest request);

    @DELETE("/api/manager/location/{id}")
    Call<Void> deleteLocation(@Path("id") int id);


    //endpoints for addons
    @GET("/api/manager/addons")
    Call<List<AddOnResponse>> getAddOns();

    @GET("/api/manager/addons/{id}")
    Call<AddOnResponse> getAddOnById(@Path("id") int id);

    @POST("/api/manager/addons")
    Call<Void> createAddOn(@Body AddOnRequest request);

    @PUT("/api/manager/addons/{id}")
    Call<Void> updateAddOn(@Path("id") int id, @Body AddOnRequest request);

    @DELETE("/api/manager/addons/{id}")
    Call<Void> deleteAddOn(@Path("id") int id);

    @PATCH("/api/manager/addons/{id}/active")
    Call<Void> updateAddOnActive(@Path("id") int id, @Body ToggleActiveRequest request);


    // SUBSCRIPTIONS
    @GET("/api/manager/subscriptions")
    Call<List<SubscriptionResponse>> getSubscriptions();

    @GET("/api/manager/subscriptions/{id}")
    Call<SubscriptionRequest> getSubscriptionById(@Path("id") int id);

    @POST("/api/manager/subscriptions")
    Call<SubscriptionRequest> createSubscription(@Body SubscriptionRequest request);

    @PUT("/api/manager/subscriptions/{id}")
    Call<SubscriptionRequest> updateSubscription(@Path("id") int id, @Body SubscriptionRequest request);

    @DELETE("/api/manager/subscriptions/{id}")
    Call<Void> deleteSubscription(@Path("id") int id);

    @PATCH("/api/manager/subscriptions/{id}/status")
    Call<SubscriptionRequest> updateSubscriptionStatus(@Path("id") int id, @Body SubscriptionStatusRequest request);

    // INVOICES
    @GET("/api/invoices/admin/all")
    Call<List<InvoiceResponse>> getAllInvoicesAdmin();

    @GET("/api/invoices/{invoiceNumber}")
    Call<InvoiceResponse> getInvoiceByNumber(@Path("invoiceNumber") String invoiceNumber);

    @POST("/api/invoices/admin")
    Call<InvoiceResponse> createInvoice(@Body InvoiceRequest request);

    @PUT("/api/invoices/admin/{invoiceNumber}")
    Call<InvoiceResponse> updateInvoice(@Path("invoiceNumber") String invoiceNumber, @Body InvoiceRequest request);

    @DELETE("/api/invoices/admin/{invoiceNumber}")
    Call<Void> deleteInvoice(@Path("invoiceNumber") String invoiceNumber);

//    this is for sales report
@GET("api/manager/reports/employee-sales")
Call<List<EmployeeSalesResponse>> getEmployeeSales();

    //service requests
    @GET("/api/manager/service-requests")
    Call<List<ServiceRequestResponse>> getServiceRequests();

    @GET("/api/manager/service-requests/{id}")
    Call<ServiceRequestResponse> getServiceRequestById(@Path("id") int id);


    @POST("/api/manager/service-requests")
    Call<Void> createServiceRequest(@Body ServiceRequestCreateUpdateRequest request);

    @PUT("/api/manager/service-requests/{id}")
    Call<Void> updateServiceRequest(
            @Path("id") int id,
            @Body ServiceRequestCreateUpdateRequest request
    );

    @DELETE("/api/manager/service-requests/{id}")
    Call<Void> deleteServiceRequest(@Path("id") int id);

    //service appointment
    @GET("/api/manager/service-requests/{requestId}/appointments")
    Call<List<ServiceAppointmentResponse>> getServiceAppointments(@Path("requestId") int requestId);

    @POST("/api/manager/service-requests/{requestId}/appointments")
    Call<ServiceAppointmentResponse> createServiceAppointment(
            @Path("requestId") int requestId,
            @Body ServiceAppointmentCreateUpdateRequest request
    );

    @PUT("/api/manager/service-requests/{requestId}/appointments/{appointmentId}")
    Call<ServiceAppointmentResponse> updateServiceAppointment(
            @Path("requestId") int requestId,
            @Path("appointmentId") int appointmentId,
            @Body ServiceAppointmentCreateUpdateRequest request
    );

    @DELETE("/api/manager/service-requests/{requestId}/appointments/{appointmentId}")
    Call<Void> deleteServiceAppointment(
            @Path("requestId") int requestId,
            @Path("appointmentId") int appointmentId
    );

    //plan features
    @GET("/api/manager/planfeatures")
    Call<List<PlanFeatureResponse>> getPlanFeatures();

    @POST("/api/manager/planfeatures")
    Call<PlanFeatureResponse> createPlanFeature(@Body PlanFeatureCreateUpdateRequest request);

    @PUT("/api/manager/planfeatures/{featureId}")
    Call<PlanFeatureResponse> updatePlanFeature(
            @Path("featureId") int featureId,
            @Body PlanFeatureCreateUpdateRequest request
    );

    @DELETE("/api/manager/planfeatures/{featureId}")
    Call<Void> deletePlanFeature(@Path("featureId") int featureId);

    //showing the logs
    @GET("/api/manager/audit")
    Call<List<ActivityLogResponse>> getActivityLogs();

    //employee
    @GET("/api/manager/employees")
    Call<List<EmployeeResponse>> getEmployees();

    @GET("/api/manager/employees/{id}")
    Call<EmployeeResponse> getEmployeeById(@Path("id") int id);

    @POST("/api/manager/employees")
    Call<CreateEmployeeResponse> createEmployee(@Body SaveEmployeeRequest request);

    @PUT("/api/manager/employees/{id}")
    Call<EmployeeResponse> updateEmployee(
            @Path("id") int id,
            @Body SaveEmployeeRequest request
    );

    @DELETE("/api/manager/employees/{id}")
    Call<Void> deleteEmployee(@Path("id") int id);

    //customers
    @GET("/api/manager/customers")
    Call<List<CustomerResponse>> getCustomers();

    @GET("/api/manager/customers/{id}")
    Call<CustomerResponse> getCustomerById(@Path("id") int id);

    @POST("/api/customers")
    Call<CreateCustomerResponse> createCustomer(@Body CreateCustomerRequest request);

    @PUT("/api/customers/{id}")
    Call<CustomerResponse> updateCustomer(
            @Path("id") int id,
            @Body CreateCustomerRequest request
    );

    @DELETE("/api/customers/{id}")
    Call<Void> deleteCustomer(@Path("id") int id);

    @GET("/api/manager/customers/{customerId}/address")
    Call<CustomerAddressResponse> getCustomerAddress(@Path("customerId") int customerId);

    @PUT("/api/manager/customers/{customerId}/address")
    Call<CustomerAddressResponse> saveCustomerAddress(
            @Path("customerId") int customerId,
            @Body SaveCustomerAddressRequest request
    );


    //plans
    @GET("/api/manager/plans")
    Call<List<PlanResponse>> getPlansManager();

    @GET("/api/manager/plans/{id}")
    Call<PlanResponse> getPlanByIdManager(@Path("id") int id);

    @POST("/api/manager/plans")
    Call<PlanResponse> createPlanManager(@Body SavePlanRequest request);

    @PUT("/api/manager/plans/{id}")
    Call<PlanResponse> updatePlanManager(
            @Path("id") int id,
            @Body SavePlanRequest request
    );

    @DELETE("/api/manager/plans/{id}")
    Call<Void> deletePlanManager(@Path("id") int id);

    //Service type
    @GET("/api/manager/servicetypes")
    Call<List<ServiceTypeResponse>> getServiceTypes();
}