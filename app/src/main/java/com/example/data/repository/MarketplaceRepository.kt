package com.example.data.repository

import com.example.data.local.AdminLogEntity
import com.example.data.local.AppDatabase
import com.example.data.local.CartItemEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.CouponEntity
import com.example.data.local.DeliveryPartnerEntity
import com.example.data.local.MarketplaceDao
import com.example.data.local.MenuItemEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.OrderEntity
import com.example.data.local.OrderItemEntity
import com.example.data.local.PlatformSettingsEntity
import com.example.data.local.RestaurantEntity
import com.example.data.local.SettlementEntity
import com.example.data.local.SupportTicketEntity
import com.example.data.local.UserEntity
import com.example.data.model.DeliveryPartnerStatus
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.StoreType
import com.example.data.model.TicketStatus
import com.example.data.model.UserRole
import com.example.data.model.VendorStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MarketplaceRepository(private val db: AppDatabase) {
    val dao: MarketplaceDao = db.marketplaceDao()

    // Flows
    val allRestaurants: Flow<List<RestaurantEntity>> = dao.getAllRestaurants()
    val approvedRestaurants: Flow<List<RestaurantEntity>> = dao.getApprovedRestaurants()
    val allMenuItems: Flow<List<MenuItemEntity>> = dao.getAllMenuItems()
    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategories()
    val allCoupons: Flow<List<CouponEntity>> = dao.getAllCoupons()
    val activeCoupons: Flow<List<CouponEntity>> = dao.getActiveCoupons()
    val allOrders: Flow<List<OrderEntity>> = dao.getAllOrders()
    val allDeliveryPartners: Flow<List<DeliveryPartnerEntity>> = dao.getAllDeliveryPartners()
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    val allSupportTickets: Flow<List<SupportTicketEntity>> = dao.getAllSupportTickets()
    val platformSettings: Flow<PlatformSettingsEntity?> = dao.getSettings()
    val adminLogs: Flow<List<AdminLogEntity>> = dao.getAdminLogs()
    val allSettlements: Flow<List<SettlementEntity>> = dao.getAllSettlements()

    fun getCartItems(userId: Long): Flow<List<CartItemEntity>> = dao.getCartItems(userId)
    fun getOrdersForCustomer(customerId: Long): Flow<List<OrderEntity>> = dao.getOrdersForCustomer(customerId)
    fun getOrdersForRestaurant(restaurantId: Long): Flow<List<OrderEntity>> = dao.getOrdersForRestaurant(restaurantId)
    fun getOrdersForDeliveryPartner(partnerId: Long): Flow<List<OrderEntity>> = dao.getOrdersForDeliveryPartner(partnerId)
    fun getAvailableDeliveryOrders(): Flow<List<OrderEntity>> = dao.getAvailableDeliveryOrders()
    fun getOrderById(orderId: Long): Flow<OrderEntity?> = dao.getOrderById(orderId)
    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>> = dao.getOrderItems(orderId)
    fun getMenuItemsByRestaurant(restaurantId: Long): Flow<List<MenuItemEntity>> = dao.getMenuItemsByRestaurant(restaurantId)
    fun getRestaurantById(id: Long): Flow<RestaurantEntity?> = dao.getRestaurantById(id)
    fun getRestaurantByOwner(ownerId: Long): Flow<RestaurantEntity?> = dao.getRestaurantByOwner(ownerId)
    fun getDeliveryPartnerByUserId(userId: Long): Flow<DeliveryPartnerEntity?> = dao.getDeliveryPartnerByUserId(userId)
    fun getTicketsForUser(userId: Long): Flow<List<SupportTicketEntity>> = dao.getTicketsForUser(userId)
    fun getNotifications(role: UserRole, userId: Long): Flow<List<NotificationEntity>> = dao.getNotificationsForRoleAndUser(role, userId)

    suspend fun checkAndSeedDatabase() = withContext(Dispatchers.IO) {
        val existingUsers = dao.getAllUsers().first()
        val masterEmail = "mandalhabibullah476@gmail.com"

        if (existingUsers.isNotEmpty()) {
            val existingMaster = existingUsers.find { it.email.equals(masterEmail, ignoreCase = true) }
            if (existingMaster == null) {
                dao.insertUser(
                    UserEntity(
                        name = "Habibullah Mandal (Master Owner)",
                        email = masterEmail,
                        phone = "+91 98765 43210",
                        role = UserRole.SUPER_ADMIN,
                        passwordHash = "master123",
                        address = "OmniFeast Global HQ, Tower A (Master Owner Office)",
                        latitude = 12.9716,
                        longitude = 77.5946
                    )
                )
            } else if (existingMaster.role != UserRole.SUPER_ADMIN) {
                dao.updateUser(existingMaster.copy(role = UserRole.SUPER_ADMIN, name = "Habibullah Mandal (Master Owner)"))
            }
            return@withContext
        }

        // 1. Seed Users for each role
        val customerId = dao.insertUser(
            UserEntity(
                name = "Alex Morgan",
                email = "customer@omnifeast.com",
                phone = "+1 (555) 234-5678",
                role = UserRole.CUSTOMER,
                passwordHash = "demo123",
                address = "742 Evergreen Terrace, Sector 4, Metro City",
                latitude = 12.9716,
                longitude = 77.5946
            )
        )

        val vendorUser1Id = dao.insertUser(
            UserEntity(
                name = "Chef Marco Rossi",
                email = "vendor@omnifeast.com",
                phone = "+1 (555) 345-6789",
                role = UserRole.RESTAURANT_OWNER,
                passwordHash = "demo123",
                address = "12 Piazza Della Woodfire, Metro City"
            )
        )

        val vendorUser2Id = dao.insertUser(
            UserEntity(
                name = "Priya Sharma",
                email = "biryani@omnifeast.com",
                phone = "+1 (555) 456-7890",
                role = UserRole.RESTAURANT_OWNER,
                passwordHash = "demo123",
                address = "88 Spice Court, Old Bazaar, Metro City"
            )
        )

        val groceryOwnerId = dao.insertUser(
            UserEntity(
                name = "David Miller",
                email = "grocery@omnifeast.com",
                phone = "+1 (555) 567-8901",
                role = UserRole.RESTAURANT_OWNER,
                passwordHash = "demo123",
                address = "45 Fresh Farms Lane, Metro City"
            )
        )

        val driverUser1Id = dao.insertUser(
            UserEntity(
                name = "Carlos Vance",
                email = "driver@omnifeast.com",
                phone = "+1 (555) 678-9012",
                role = UserRole.DELIVERY_PARTNER,
                passwordHash = "demo123",
                address = "21 Rider Way, Metro City"
            )
        )

        dao.insertUser(
            UserEntity(
                name = "Admin Staff",
                email = "admin@omnifeast.com",
                phone = "+1 (555) 789-0123",
                role = UserRole.ADMIN_STAFF,
                passwordHash = "demo123",
                address = "OmniFeast HQ, Suite 400"
            )
        )

        dao.insertUser(
            UserEntity(
                name = "Habibullah Mandal (Master Owner)",
                email = "mandalhabibullah476@gmail.com",
                phone = "+91 98765 43210",
                role = UserRole.SUPER_ADMIN,
                passwordHash = "master123",
                address = "OmniFeast Global HQ, Tower A (Master Owner Office)",
                latitude = 12.9716,
                longitude = 77.5946
            )
        )

        // 2. Seed Platform Settings
        dao.insertOrUpdateSettings(
            PlatformSettingsEntity(
                id = 1,
                restaurantCommissionPercent = 15.0,
                baseDeliveryFee = 25.0,
                perKmFee = 8.0,
                platformFee = 5.0,
                taxRatePercent = 5.0,
                freeDeliveryThreshold = 499.0,
                isPlatformActive = true,
                currencySymbol = "₹"
            )
        )

        // 3. Seed Categories
        val categories = listOf(
            CategoryEntity(name = "All", storeType = StoreType.FOOD_RESTAURANT, iconName = "restaurant", sortOrder = 0),
            CategoryEntity(name = "Biryani", storeType = StoreType.FOOD_RESTAURANT, iconName = "rice", sortOrder = 1),
            CategoryEntity(name = "Pizza & Pasta", storeType = StoreType.FOOD_RESTAURANT, iconName = "pizza", sortOrder = 2),
            CategoryEntity(name = "Burgers", storeType = StoreType.FOOD_RESTAURANT, iconName = "burger", sortOrder = 3),
            CategoryEntity(name = "Asian & Sushi", storeType = StoreType.FOOD_RESTAURANT, iconName = "ramen", sortOrder = 4),
            CategoryEntity(name = "Fresh Produce", storeType = StoreType.GROCERY_MART, iconName = "produce", sortOrder = 5),
            CategoryEntity(name = "Dairy & Bakery", storeType = StoreType.GROCERY_MART, iconName = "dairy", sortOrder = 6),
            CategoryEntity(name = "Drinks & Snacks", storeType = StoreType.GROCERY_MART, iconName = "beverage", sortOrder = 7),
            CategoryEntity(name = "Desserts", storeType = StoreType.FOOD_RESTAURANT, iconName = "icecream", sortOrder = 8)
        )
        categories.forEach { dao.insertCategory(it) }

        // 4. Seed Restaurants & Stores
        val r1Id = dao.insertRestaurant(
            RestaurantEntity(
                ownerId = vendorUser1Id,
                name = "Bella Napoli Woodfire Pizzeria",
                storeType = StoreType.FOOD_RESTAURANT,
                cuisineOrCategory = "Italian • Woodfire Pizza • Pasta",
                address = "12 Piazza Della Woodfire, Downtown",
                latitude = 12.9780,
                longitude = 77.5990,
                rating = 4.8,
                reviewCount = 342,
                deliveryRadiusKm = 8.0,
                minOrder = 200.0,
                prepTimeMinutes = 25,
                isOpen = true,
                status = VendorStatus.APPROVED,
                foodLicense = "FSSAI-110022339944",
                totalSales = 28400.0,
                totalEarnings = 24140.0,
                pendingSettlement = 4200.0
            )
        )

        val r2Id = dao.insertRestaurant(
            RestaurantEntity(
                ownerId = vendorUser2Id,
                name = "Royal Dum Biryani & Kebabs",
                storeType = StoreType.FOOD_RESTAURANT,
                cuisineOrCategory = "Mughlai • Hyderabadi Biryani • Kebabs",
                address = "88 Spice Court, Old Bazaar",
                latitude = 12.9650,
                longitude = 77.5890,
                rating = 4.9,
                reviewCount = 890,
                deliveryRadiusKm = 12.0,
                minOrder = 180.0,
                prepTimeMinutes = 30,
                isOpen = true,
                status = VendorStatus.APPROVED,
                foodLicense = "FSSAI-220044883311",
                totalSales = 45900.0,
                totalEarnings = 39015.0,
                pendingSettlement = 6800.0
            )
        )

        val r3Id = dao.insertRestaurant(
            RestaurantEntity(
                ownerId = groceryOwnerId,
                name = "GreenFresh Organic Supermarket",
                storeType = StoreType.GROCERY_MART,
                cuisineOrCategory = "Fresh Fruits • Vegetables • Organic Dairy",
                address = "45 Fresh Farms Lane, Central Avenue",
                latitude = 12.9730,
                longitude = 77.5910,
                rating = 4.7,
                reviewCount = 420,
                deliveryRadiusKm = 10.0,
                minOrder = 150.0,
                prepTimeMinutes = 15,
                isOpen = true,
                status = VendorStatus.APPROVED,
                foodLicense = "FSSAI-330055112277",
                totalSales = 36500.0,
                totalEarnings = 31025.0,
                pendingSettlement = 5100.0
            )
        )

        val r4Id = dao.insertRestaurant(
            RestaurantEntity(
                ownerId = 0,
                name = "Urban Smash Burgers & Shakes",
                storeType = StoreType.FOOD_RESTAURANT,
                cuisineOrCategory = "American • Gourmet Burgers • Loaded Fries",
                address = "104 Indie Boulevard, High Street",
                latitude = 12.9820,
                longitude = 77.6020,
                rating = 4.6,
                reviewCount = 215,
                deliveryRadiusKm = 9.0,
                minOrder = 150.0,
                prepTimeMinutes = 20,
                isOpen = true,
                status = VendorStatus.APPROVED,
                foodLicense = "FSSAI-440066228833"
            )
        )

        val r5Id = dao.insertRestaurant(
            RestaurantEntity(
                ownerId = 0,
                name = "Daily Express 15-Min Pantry",
                storeType = StoreType.GROCERY_MART,
                cuisineOrCategory = "Snacks • Instant Meals • Cold Beverages",
                address = "Shop 3, QuickHub Station, Tech Park",
                latitude = 12.9690,
                longitude = 77.5960,
                rating = 4.8,
                reviewCount = 612,
                deliveryRadiusKm = 5.0,
                minOrder = 99.0,
                prepTimeMinutes = 10,
                isOpen = true,
                status = VendorStatus.APPROVED,
                foodLicense = "FSSAI-550077339944"
            )
        )

        // Pending Restaurant application for Admin approval testing
        dao.insertRestaurant(
            RestaurantEntity(
                ownerId = 0,
                name = "Sakura Tokyo Ramen & Bento",
                storeType = StoreType.FOOD_RESTAURANT,
                cuisineOrCategory = "Japanese • Ramen • Gyoza • Bento",
                address = "52 Sakura Street, West Wing",
                latitude = 12.9850,
                longitude = 77.5850,
                rating = 4.5,
                reviewCount = 0,
                deliveryRadiusKm = 10.0,
                minOrder = 250.0,
                prepTimeMinutes = 25,
                isOpen = false,
                status = VendorStatus.PENDING,
                foodLicense = "FSSAI-990011223344"
            )
        )

        // 5. Seed Menu Items
        // Bella Napoli
        dao.insertMenuItem(MenuItemEntity(restaurantId = r1Id, categoryName = "Pizza & Pasta", name = "Margherita Gourmet Woodfire Pizza", description = "San Marzano tomatoes, fresh buffalo mozzarella, fragrant sweet basil, extra virgin olive oil.", price = 349.0, discountPrice = 299.0, isVeg = true, variations = "Regular (10\"), Large (12\")", addons = "Extra Burrata (+₹80), Garlic Dip (+₹30)"))
        dao.insertMenuItem(MenuItemEntity(restaurantId = r1Id, categoryName = "Pizza & Pasta", name = "Truffle Mushroom & Smoked Cheese Pizza", description = "Wild button & porcini mushrooms, creamy fontina, truffle drizzle, fresh thyme.", price = 449.0, discountPrice = null, isVeg = true, variations = "Regular (10\"), Large (12\")", addons = "Extra Truffle Glaze (+₹60), Olives (+₹40)"))
        dao.insertMenuItem(MenuItemEntity(restaurantId = r1Id, categoryName = "Pizza & Pasta", name = "Classic Chicken Pepperoni Passion", description = "Spicy smoky chicken pepperoni, roasted red bell peppers, charred garlic crust.", price = 499.0, discountPrice = 449.0, isVeg = false, variations = "Regular (10\"), Large (12\")", addons = "Jalapenos (+₹30), Cheese Burst (+₹90)"))
        dao.insertMenuItem(MenuItemEntity(restaurantId = r1Id, categoryName = "Pizza & Pasta", name = "Creamy Pesto Penne Genovese", description = "Fresh handmade basil pesto, toasted pine nuts, shaved parmesan, garlic croutons.", price = 329.0, isVeg = true))
        dao.insertMenuItem(MenuItemEntity(restaurantId = r1Id, categoryName = "Desserts", name = "Traditional Espresso Tiramisu", description = "Savoiardi ladyfingers soaked in dark roast espresso and mascarpone cream.", price = 220.0, isVeg = true))

        // Royal Biryani
        dao.insertMenuItem(MenuItemEntity(restaurantId = r2Id, categoryName = "Biryani", name = "Hyderabadi Shahi Chicken Dum Biryani", description = "Slow-cooked aromatic basmati rice layered with succulent chicken, saffron, and fried onions.", price = 360.0, discountPrice = 319.0, isVeg = false, variations = "Single Pack, Handi Sharing Pack (2-3)", addons = "Mirchi Ka Salan (+₹30), Extra Boiled Egg (+₹20), Spicy Raita (+₹25)"))
        dao.insertMenuItem(MenuItemEntity(restaurantId = r2Id, categoryName = "Biryani", name = "Nawabi Mutton Dum Biryani (Spicy)", description = "Tender baby goat pieces cooked in sealed handi with royal fragrant herbs and brown onions.", price = 490.0, discountPrice = 450.0, isVeg = false, variations = "Single Pack, Royal Handi", addons = "Extra Mutton Piece (+₹120), Boondi Raita (+₹30)"))
        dao.insertMenuItem(MenuItemEntity(restaurantId = r2Id, categoryName = "Biryani", name = "Subz Paneer Tikka Dum Biryani", description = "Charred cottage cheese cubes, baby potatoes, green peas in rich aromatic saffron rice.", price = 299.0, isVeg = true, variations = "Regular, Jumbo", addons = "Roasted Papad (+₹20), Mint Chutney (+₹15)"))
        dao.insertMenuItem(MenuItemEntity(restaurantId = r2Id, categoryName = "Biryani", name = "Murgh Malai Reshmi Kebabs (6 Pcs)", description = "Melt-in-mouth chicken boneless skewers infused with green cardamom, cashew cream, and herbs.", price = 340.0, isVeg = false))
        dao.insertMenuItem(MenuItemEntity(restaurantId = r2Id, categoryName = "Desserts", name = "Shahi Zafrani Phirni", description = "Thick ground rice pudding slow simmered with whole milk, saffron, and crushed pistachios.", price = 140.0, isVeg = true))

        // GreenFresh Organics
        dao.insertMenuItem(MenuItemEntity(restaurantId = r3Id, categoryName = "Fresh Produce", name = "Organic Shimla Apples (1 Kg)", description = "Crisp, naturally sweet organic mountain apples harvested fresh this week.", price = 180.0, discountPrice = 159.0, isVeg = true))
        dao.insertMenuItem(MenuItemEntity(restaurantId = r3Id, categoryName = "Fresh Produce", name = "Hydroponic Salad Greens Box (250g)", description = "Fresh baby spinach, cherry tomatoes, butterhead lettuce, and arugula.", price = 120.0, isVeg = true))
        dao.insertMenuItem(MenuItemEntity(restaurantId = r3Id, categoryName = "Fresh Produce", name = "Hass Avocados (2 Pcs Pack)", description = "Creamy, ready-to-eat Mexican variety avocados rich in healthy fats.", price = 240.0, discountPrice = 199.0, isVeg = true))
        dao.insertMenuItem(MenuItemEntity(restaurantId = r3Id, categoryName = "Dairy & Bakery", name = "Farm Fresh Organic A2 Whole Milk (1L)", description = "Pasteurized, unhomogenized pure organic cow milk in eco-friendly glass bottle.", price = 95.0, isVeg = true))
        dao.insertMenuItem(MenuItemEntity(restaurantId = r3Id, categoryName = "Dairy & Bakery", name = "Artisan Sourdough Country Loaf (400g)", description = "Slow fermented 36-hour wild yeast sourdough with crunchy golden crust.", price = 150.0, isVeg = true))

        // Urban Smash Burgers
        dao.insertMenuItem(MenuItemEntity(restaurantId = r4Id, categoryName = "Burgers", name = "Double Truffle Smash Cheeseburger", description = "Two smashed crispy patties, melted cheddar, caramelized onions, truffle aioli on brioche.", price = 289.0, discountPrice = 249.0, isVeg = false, variations = "Single Patty, Double Smash", addons = "Crispy Bacon Strips (+₹60), Jalapeno Poppers (+₹50)"))
        dao.insertMenuItem(MenuItemEntity(restaurantId = r4Id, categoryName = "Burgers", name = "Crispy Paneer Makhani Burger", description = "Crispy golden paneer patty glazed with rich buttery makhani sauce and spiced slaw.", price = 219.0, isVeg = true, addons = "Peri Peri Seasoning (+₹20)"))
        dao.insertMenuItem(MenuItemEntity(restaurantId = r4Id, categoryName = "Drinks & Snacks", name = "Peri-Peri Seasoned Crinkle Fries", description = "Golden crispy crinkle cut potatoes tossed in signature African bird's eye spice.", price = 129.0, isVeg = true))
        dao.insertMenuItem(MenuItemEntity(restaurantId = r4Id, categoryName = "Drinks & Snacks", name = "Salted Caramel Thick Milkshake", description = "Blended Madagascar vanilla bean gelato, fleur de sel, buttery dulce de leche swirl.", price = 179.0, isVeg = true))

        // Daily Express
        dao.insertMenuItem(MenuItemEntity(restaurantId = r5Id, categoryName = "Drinks & Snacks", name = "Cold Pressed Tender Coconut Water (300ml)", description = "100% natural, electrolyte-rich chilled coconut water with no added sugars.", price = 65.0, isVeg = true))
        dao.insertMenuItem(MenuItemEntity(restaurantId = r5Id, categoryName = "Drinks & Snacks", name = "Crunchy Roasted Almonds & Sea Salt (200g)", description = "California almonds roasted to perfection and lightly sprinkled with pink rock salt.", price = 220.0, discountPrice = 189.0, isVeg = true))

        // 6. Seed Delivery Partner
        val driverPartnerId = dao.insertDeliveryPartner(
            DeliveryPartnerEntity(
                userId = driverUser1Id,
                name = "Carlos Vance",
                phone = "+1 (555) 678-9012",
                email = "driver@omnifeast.com",
                vehicleType = "Electric Scooter",
                vehicleNumber = "KA-01-EF-5566",
                licenseNumber = "IND-DL-88331199",
                kycStatus = DeliveryPartnerStatus.APPROVED,
                isOnline = true,
                currentLat = 12.9740,
                currentLng = 77.5930,
                totalDeliveries = 184,
                rating = 4.85,
                walletBalance = 1620.0
            )
        )

        // Pending Driver for Admin review testing
        dao.insertDeliveryPartner(
            DeliveryPartnerEntity(
                userId = 0,
                name = "Vikram Reddy",
                phone = "+1 (555) 998-1122",
                email = "vikram.delivery@omnifeast.com",
                vehicleType = "Motorcycle 150cc",
                vehicleNumber = "KA-05-XY-8899",
                licenseNumber = "IND-DL-44556677",
                kycStatus = DeliveryPartnerStatus.PENDING,
                isOnline = false,
                currentLat = 12.9650,
                currentLng = 77.5850,
                totalDeliveries = 0,
                rating = 5.0,
                walletBalance = 0.0
            )
        )

        // 7. Seed Coupons
        dao.insertCoupon(CouponEntity(code = "FEAST50", title = "50% Off Welcome Feast", description = "Get 50% discount up to ₹120 on orders above ₹249", discountPercent = 50, maxDiscount = 120.0, minOrderAmount = 249.0))
        dao.insertCoupon(CouponEntity(code = "FREEDEL", title = "Zero Delivery Fee", description = "Enjoy free delivery on your favorite meals and groceries", discountPercent = 100, maxDiscount = 40.0, minOrderAmount = 199.0))
        dao.insertCoupon(CouponEntity(code = "SUPER100", title = "Flat ₹100 Off", description = "Save flat ₹100 on weekend feasts above ₹399", discountPercent = 25, maxDiscount = 100.0, minOrderAmount = 399.0))
        dao.insertCoupon(CouponEntity(code = "GROCERY20", title = "Pantry Saver 20%", description = "Stock up your kitchen with 20% off up to ₹150", discountPercent = 20, maxDiscount = 150.0, minOrderAmount = 299.0))

        // 8. Seed Live / Past Orders for Customer & Delivery demonstration
        val activeOrderId = dao.insertOrder(
            OrderEntity(
                orderNumber = "ORD-FEAST-9021",
                customerId = customerId,
                customerName = "Alex Morgan",
                customerPhone = "+1 (555) 234-5678",
                deliveryAddress = "742 Evergreen Terrace, Sector 4, Metro City",
                customerLat = 12.9716,
                customerLng = 77.5946,
                restaurantId = r1Id,
                restaurantName = "Bella Napoli Woodfire Pizzeria",
                restaurantAddress = "12 Piazza Della Woodfire, Downtown",
                restaurantLat = 12.9780,
                restaurantLng = 77.5990,
                deliveryPartnerId = driverPartnerId,
                deliveryPartnerName = "Carlos Vance",
                deliveryPartnerPhone = "+1 (555) 678-9012",
                status = OrderStatus.OUT_FOR_DELIVERY,
                itemTotal = 748.0,
                deliveryFee = 25.0,
                taxes = 37.4,
                platformFee = 5.0,
                discountAmount = 120.0,
                couponCode = "FEAST50",
                totalAmount = 695.4,
                paymentMethod = PaymentMethod.ONLINE_UPI,
                paymentStatus = PaymentStatus.SUCCESSFUL,
                deliveryOtp = "4812",
                orderTimestamp = System.currentTimeMillis() - (18 * 60 * 1000),
                estimatedMinutes = 12
            )
        )

        dao.insertOrderItems(
            listOf(
                OrderItemEntity(orderId = activeOrderId, menuItemId = 1, name = "Margherita Gourmet Woodfire Pizza", price = 299.0, quantity = 1, isVeg = true, customization = "Large (12\"), Extra Burrata"),
                OrderItemEntity(orderId = activeOrderId, menuItemId = 4, name = "Creamy Pesto Penne Genovese", price = 329.0, quantity = 1, isVeg = true, customization = "Standard"),
                OrderItemEntity(orderId = activeOrderId, menuItemId = 5, name = "Traditional Espresso Tiramisu", price = 120.0, quantity = 1, isVeg = true, customization = "Chilled")
            )
        )

        // Completed Past Order
        val completedOrderId = dao.insertOrder(
            OrderEntity(
                orderNumber = "ORD-FEAST-8814",
                customerId = customerId,
                customerName = "Alex Morgan",
                customerPhone = "+1 (555) 234-5678",
                deliveryAddress = "742 Evergreen Terrace, Sector 4, Metro City",
                customerLat = 12.9716,
                customerLng = 77.5946,
                restaurantId = r2Id,
                restaurantName = "Royal Dum Biryani & Kebabs",
                restaurantAddress = "88 Spice Court, Old Bazaar",
                restaurantLat = 12.9650,
                restaurantLng = 77.5890,
                deliveryPartnerId = driverPartnerId,
                deliveryPartnerName = "Carlos Vance",
                deliveryPartnerPhone = "+1 (555) 678-9012",
                status = OrderStatus.DELIVERED,
                itemTotal = 659.0,
                deliveryFee = 0.0,
                taxes = 32.95,
                platformFee = 5.0,
                discountAmount = 40.0,
                couponCode = "FREEDEL",
                totalAmount = 656.95,
                paymentMethod = PaymentMethod.ONLINE_CARD,
                paymentStatus = PaymentStatus.SUCCESSFUL,
                deliveryOtp = "7731",
                orderTimestamp = System.currentTimeMillis() - (2 * 86400 * 1000),
                estimatedMinutes = 0
            )
        )

        dao.insertOrderItems(
            listOf(
                OrderItemEntity(orderId = completedOrderId, menuItemId = 6, name = "Hyderabadi Shahi Chicken Dum Biryani", price = 319.0, quantity = 1, isVeg = false, customization = "Single Pack, Extra Raita"),
                OrderItemEntity(orderId = completedOrderId, menuItemId = 9, name = "Murgh Malai Reshmi Kebabs (6 Pcs)", price = 340.0, quantity = 1, isVeg = false)
            )
        )

        // 9. Seed Notifications
        dao.insertNotification(
            NotificationEntity(
                targetRole = UserRole.CUSTOMER,
                targetUserId = customerId,
                title = "Order Out for Delivery! 🛵",
                message = "Carlos Vance is on his way with your hot meal from Bella Napoli. Share OTP 4812 upon delivery.",
                relatedOrderId = activeOrderId
            )
        )

        dao.insertNotification(
            NotificationEntity(
                targetRole = UserRole.RESTAURANT_OWNER,
                targetUserId = vendorUser1Id,
                title = "Order #9021 Dispatched",
                message = "Rider Carlos Vance picked up order #9021. Kitchen prep time: 14 mins.",
                relatedOrderId = activeOrderId
            )
        )

        dao.insertNotification(
            NotificationEntity(
                targetRole = UserRole.ADMIN_STAFF,
                title = "New Vendor KYC Pending Review",
                message = "Sakura Tokyo Ramen submitted documents for Super Admin approval."
            )
        )

        // 10. Seed Support Ticket
        dao.insertSupportTicket(
            SupportTicketEntity(
                userId = customerId,
                userName = "Alex Morgan",
                userRole = UserRole.CUSTOMER,
                orderId = completedOrderId,
                subject = "Extra Raita was packed neatly! Great service.",
                message = "I just wanted to appreciate the prompt delivery and packaging quality.",
                status = TicketStatus.RESOLVED,
                adminReply = "Thank you Alex! We have forwarded your compliments to Royal Dum Biryani."
            )
        )

        // 11. Seed Admin Log
        dao.insertAdminLog(
            AdminLogEntity(
                adminName = "Super Admin",
                action = "Platform Initialized",
                details = "OmniFeast multi-vendor marketplace engine started with core tables, restaurants, categories, and settings."
            )
        )
    }

    // Business Methods
    suspend fun addToCart(
        userId: Long,
        restaurantId: Long,
        menuItemId: Long,
        name: String,
        price: Double,
        isVeg: Boolean,
        variation: String = "",
        addons: String = ""
    ) = withContext(Dispatchers.IO) {
        val existing = dao.getCartItems(userId).first()
        // If items are from a different restaurant, clear first (standard marketplace UX)
        if (existing.isNotEmpty() && existing.first().restaurantId != restaurantId) {
            dao.clearCart(userId)
        }
        val match = existing.find { it.menuItemId == menuItemId && it.selectedVariation == variation && it.selectedAddons == addons }
        if (match != null) {
            dao.updateCartItemQuantity(match.id, match.quantity + 1)
        } else {
            dao.insertCartItem(
                CartItemEntity(
                    userId = userId,
                    restaurantId = restaurantId,
                    menuItemId = menuItemId,
                    name = name,
                    price = price,
                    quantity = 1,
                    isVeg = isVeg,
                    selectedVariation = variation,
                    selectedAddons = addons
                )
            )
        }
    }

    suspend fun updateCartQuantity(cartItemId: Long, newQuantity: Int) = withContext(Dispatchers.IO) {
        if (newQuantity <= 0) {
            dao.deleteCartItem(cartItemId)
        } else {
            dao.updateCartItemQuantity(cartItemId, newQuantity)
        }
    }

    suspend fun clearCart(userId: Long) = withContext(Dispatchers.IO) {
        dao.clearCart(userId)
    }

    suspend fun placeOrder(
        customerId: Long,
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        customerLat: Double,
        customerLng: Double,
        restaurant: RestaurantEntity,
        cartItems: List<CartItemEntity>,
        itemTotal: Double,
        deliveryFee: Double,
        taxes: Double,
        platformFee: Double,
        discountAmount: Double,
        couponCode: String?,
        totalAmount: Double,
        paymentMethod: PaymentMethod
    ): Long = withContext(Dispatchers.IO) {
        val orderNo = "ORD-FEAST-" + (1000..9999).random()
        val otp = (1000..9999).random().toString()
        val paymentStatus = if (paymentMethod == PaymentMethod.CASH_ON_DELIVERY) PaymentStatus.PENDING else PaymentStatus.SUCCESSFUL

        val order = OrderEntity(
            orderNumber = orderNo,
            customerId = customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = customerAddress,
            customerLat = customerLat,
            customerLng = customerLng,
            restaurantId = restaurant.id,
            restaurantName = restaurant.name,
            restaurantAddress = restaurant.address,
            restaurantLat = restaurant.latitude,
            restaurantLng = restaurant.longitude,
            status = OrderStatus.PLACED,
            itemTotal = itemTotal,
            deliveryFee = deliveryFee,
            taxes = taxes,
            platformFee = platformFee,
            discountAmount = discountAmount,
            couponCode = couponCode,
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            deliveryOtp = otp,
            estimatedMinutes = restaurant.prepTimeMinutes + 15
        )

        val orderId = dao.insertOrder(order)
        val orderItems = cartItems.map {
            OrderItemEntity(
                orderId = orderId,
                menuItemId = it.menuItemId,
                name = it.name,
                price = it.price,
                quantity = it.quantity,
                isVeg = it.isVeg,
                customization = listOfNotNull(it.selectedVariation.takeIf { v -> v.isNotEmpty() }, it.selectedAddons.takeIf { a -> a.isNotEmpty() }).joinToString(", ")
            )
        }
        dao.insertOrderItems(orderItems)
        dao.clearCart(customerId)

        // Send notifications
        dao.insertNotification(
            NotificationEntity(
                targetRole = UserRole.CUSTOMER,
                targetUserId = customerId,
                title = "Order Placed Successfully! 🎉",
                message = "Order $orderNo has been sent to ${restaurant.name}.",
                relatedOrderId = orderId
            )
        )
        dao.insertNotification(
            NotificationEntity(
                targetRole = UserRole.RESTAURANT_OWNER,
                targetUserId = restaurant.ownerId,
                title = "New Order Incoming! 🔔",
                message = "New order $orderNo worth ₹${String.format(Locale.US, "%.2f", totalAmount)} received.",
                relatedOrderId = orderId
            )
        )

        // Log
        dao.insertAdminLog(
            AdminLogEntity(
                adminName = customerName,
                action = "Order Placed",
                details = "$orderNo at ${restaurant.name} for ₹${String.format(Locale.US, "%.2f", totalAmount)}"
            )
        )

        orderId
    }

    suspend fun updateOrderStatus(orderId: Long, newStatus: OrderStatus, adminOrVendorName: String) = withContext(Dispatchers.IO) {
        dao.updateOrderStatus(orderId, newStatus)
        val order = dao.getOrderById(orderId).first() ?: return@withContext

        // Notify customer
        val title = when (newStatus) {
            OrderStatus.RESTAURANT_ACCEPTED -> "Restaurant Accepted Your Order! 👨‍🍳"
            OrderStatus.PREPARING -> "Chef is Preparing Your Food 🍲"
            OrderStatus.READY_FOR_PICKUP -> "Order is Packed & Ready for Pickup 📦"
            OrderStatus.DRIVER_ASSIGNED -> "Delivery Partner Assigned 🛵"
            OrderStatus.OUT_FOR_DELIVERY -> "Out for Delivery! 🚀"
            OrderStatus.DELIVERED -> "Order Delivered! Enjoy your meal 😋"
            OrderStatus.CANCELLED -> "Order Cancelled ❌"
            OrderStatus.REFUND_REQUESTED -> "Refund Request Received ⏳"
            OrderStatus.REFUNDED -> "Refund Processed Successfully 💰"
            else -> "Order Update: ${newStatus.label}"
        }

        dao.insertNotification(
            NotificationEntity(
                targetRole = UserRole.CUSTOMER,
                targetUserId = order.customerId,
                title = title,
                message = "Order ${order.orderNumber} status changed to ${newStatus.label}.",
                relatedOrderId = orderId
            )
        )

        dao.insertAdminLog(
            AdminLogEntity(
                adminName = adminOrVendorName,
                action = "Order Status Updated",
                details = "${order.orderNumber} -> ${newStatus.label}"
            )
        )
    }

    suspend fun assignDriverToOrder(orderId: Long, driver: DeliveryPartnerEntity) = withContext(Dispatchers.IO) {
        dao.assignDeliveryPartner(orderId, driver.id, driver.name, driver.phone)
        val order = dao.getOrderById(orderId).first() ?: return@withContext
        dao.insertNotification(
            NotificationEntity(
                targetRole = UserRole.CUSTOMER,
                targetUserId = order.customerId,
                title = "Delivery Partner Assigned 🛵",
                message = "${driver.name} has accepted delivery for order ${order.orderNumber}.",
                relatedOrderId = orderId
            )
        )
    }

    suspend fun verifyAndCompleteDelivery(orderId: Long, enteredOtp: String): Boolean = withContext(Dispatchers.IO) {
        val order = dao.getOrderById(orderId).first() ?: return@withContext false
        if (order.deliveryOtp.trim() == enteredOtp.trim()) {
            dao.updateOrderStatus(orderId, OrderStatus.DELIVERED)
            // Add delivery payout to driver
            order.deliveryPartnerId?.let { partnerId ->
                val partner = dao.getDeliveryPartnerById(partnerId).first()
                if (partner != null) {
                    dao.updateDeliveryPartner(
                        partner.copy(
                            walletBalance = partner.walletBalance + 45.0,
                            totalDeliveries = partner.totalDeliveries + 1
                        )
                    )
                }
            }
            dao.insertNotification(
                NotificationEntity(
                    targetRole = UserRole.CUSTOMER,
                    targetUserId = order.customerId,
                    title = "Order Delivered! 🎉",
                    message = "Your order ${order.orderNumber} was marked delivered. Thank you for using OmniFeast!",
                    relatedOrderId = orderId
                )
            )
            true
        } else {
            false
        }
    }

    suspend fun cancelOrder(orderId: Long, reason: String, userName: String) = withContext(Dispatchers.IO) {
        dao.cancelOrder(orderId, reason)
        dao.insertAdminLog(
            AdminLogEntity(
                adminName = userName,
                action = "Order Cancelled",
                details = "Order #$orderId cancelled. Reason: $reason"
            )
        )
    }

    suspend fun requestRefund(orderId: Long, reason: String, userName: String) = withContext(Dispatchers.IO) {
        dao.requestRefund(orderId, reason)
        dao.insertAdminLog(
            AdminLogEntity(
                adminName = userName,
                action = "Refund Requested",
                details = "Order #$orderId. Reason: $reason"
            )
        )
    }

    suspend fun processRefund(orderId: Long, adminName: String) = withContext(Dispatchers.IO) {
        dao.processRefund(orderId)
        dao.insertAdminLog(
            AdminLogEntity(
                adminName = adminName,
                action = "Refund Processed",
                details = "Order #$orderId refund marked successful."
            )
        )
    }

    // Vendor Management
    suspend fun registerRestaurant(
        ownerId: Long,
        name: String,
        storeType: StoreType,
        cuisine: String,
        address: String,
        foodLicense: String,
        bankAccount: String
    ): Long = withContext(Dispatchers.IO) {
        val r = RestaurantEntity(
            ownerId = ownerId,
            name = name,
            storeType = storeType,
            cuisineOrCategory = cuisine,
            address = address,
            latitude = 12.9716 + (Math.random() - 0.5) * 0.04,
            longitude = 77.5946 + (Math.random() - 0.5) * 0.04,
            status = VendorStatus.PENDING,
            foodLicense = foodLicense,
            bankAccount = bankAccount,
            isOpen = false
        )
        val id = dao.insertRestaurant(r)
        dao.insertAdminLog(
            AdminLogEntity(
                adminName = name,
                action = "Vendor Registered",
                details = "New restaurant $name submitted KYC for approval."
            )
        )
        id
    }

    suspend fun updateRestaurantStatus(id: Long, status: VendorStatus, adminName: String) = withContext(Dispatchers.IO) {
        dao.updateRestaurantStatus(id, status)
        dao.insertAdminLog(
            AdminLogEntity(
                adminName = adminName,
                action = "Vendor Status Changed",
                details = "Restaurant #$id status set to ${status.name}"
            )
        )
    }

    suspend fun toggleRestaurantOpen(id: Long, isOpen: Boolean) = withContext(Dispatchers.IO) {
        dao.updateRestaurantOpenStatus(id, isOpen)
    }

    suspend fun addMenuItem(
        restaurantId: Long,
        categoryName: String,
        name: String,
        description: String,
        price: Double,
        discountPrice: Double?,
        isVeg: Boolean,
        variations: String,
        addons: String
    ) = withContext(Dispatchers.IO) {
        dao.insertMenuItem(
            MenuItemEntity(
                restaurantId = restaurantId,
                categoryName = categoryName,
                name = name,
                description = description,
                price = price,
                discountPrice = discountPrice,
                isVeg = isVeg,
                variations = variations,
                addons = addons
            )
        )
    }

    suspend fun updateMenuItemAvailability(id: Long, isAvailable: Boolean) = withContext(Dispatchers.IO) {
        dao.updateMenuItemAvailability(id, isAvailable)
    }

    suspend fun deleteMenuItem(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteMenuItem(id)
    }

    // Driver Management
    suspend fun registerDeliveryPartner(
        userId: Long,
        name: String,
        phone: String,
        email: String,
        vehicleType: String,
        vehicleNumber: String,
        licenseNumber: String,
        bankAccount: String
    ): Long = withContext(Dispatchers.IO) {
        val dp = DeliveryPartnerEntity(
            userId = userId,
            name = name,
            phone = phone,
            email = email,
            vehicleType = vehicleType,
            vehicleNumber = vehicleNumber,
            licenseNumber = licenseNumber,
            bankAccount = bankAccount,
            kycStatus = DeliveryPartnerStatus.PENDING,
            isOnline = false
        )
        val id = dao.insertDeliveryPartner(dp)
        dao.insertAdminLog(
            AdminLogEntity(
                adminName = name,
                action = "Driver Registered",
                details = "New driver $name submitted documents for KYC approval."
            )
        )
        id
    }

    suspend fun updateDriverStatus(id: Long, status: DeliveryPartnerStatus, adminName: String) = withContext(Dispatchers.IO) {
        dao.updateDeliveryPartnerStatus(id, status)
        dao.insertAdminLog(
            AdminLogEntity(
                adminName = adminName,
                action = "Driver Status Changed",
                details = "Delivery Partner #$id status set to ${status.name}"
            )
        )
    }

    suspend fun toggleDriverOnline(id: Long, isOnline: Boolean) = withContext(Dispatchers.IO) {
        dao.updateDeliveryPartnerOnline(id, isOnline)
    }

    // Coupons
    suspend fun addCoupon(
        code: String,
        title: String,
        description: String,
        discountPercent: Int,
        maxDiscount: Double,
        minOrderAmount: Double
    ) = withContext(Dispatchers.IO) {
        dao.insertCoupon(
            CouponEntity(
                code = code.uppercase(),
                title = title,
                description = description,
                discountPercent = discountPercent,
                maxDiscount = maxDiscount,
                minOrderAmount = minOrderAmount
            )
        )
    }

    suspend fun deleteCoupon(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteCoupon(id)
    }

    // Support
    suspend fun createSupportTicket(
        userId: Long,
        userName: String,
        userRole: UserRole,
        orderId: Long?,
        subject: String,
        message: String
    ): Long = withContext(Dispatchers.IO) {
        dao.insertSupportTicket(
            SupportTicketEntity(
                userId = userId,
                userName = userName,
                userRole = userRole,
                orderId = orderId,
                subject = subject,
                message = message
            )
        )
    }

    suspend fun replySupportTicket(ticketId: Long, reply: String, status: TicketStatus, adminName: String) = withContext(Dispatchers.IO) {
        dao.replySupportTicket(ticketId, reply, status)
        dao.insertAdminLog(
            AdminLogEntity(
                adminName = adminName,
                action = "Support Ticket Resolved",
                details = "Ticket #$ticketId updated to ${status.name}"
            )
        )
    }

    // Platform Settings
    suspend fun updateSettings(
        commissionPercent: Double,
        baseDeliveryFee: Double,
        perKmFee: Double,
        platformFee: Double,
        freeDeliveryAbove: Double,
        currency: String,
        adminName: String
    ) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateSettings(
            PlatformSettingsEntity(
                id = 1,
                restaurantCommissionPercent = commissionPercent,
                baseDeliveryFee = baseDeliveryFee,
                perKmFee = perKmFee,
                platformFee = platformFee,
                freeDeliveryThreshold = freeDeliveryAbove,
                currencySymbol = currency
            )
        )
        dao.insertAdminLog(
            AdminLogEntity(
                adminName = adminName,
                action = "Platform Fees Configured",
                details = "Commission: $commissionPercent%, Base: $baseDeliveryFee, Platform Fee: $platformFee"
            )
        )
    }

    // Settlements
    suspend fun requestSettlement(
        recipientType: String,
        recipientId: Long,
        recipientName: String,
        amount: Double
    ) = withContext(Dispatchers.IO) {
        val ref = "SETTLE-" + SimpleDateFormat("yyyyMMdd", Locale.US).format(Date()) + "-" + (1000..9999).random()
        dao.insertSettlement(
            SettlementEntity(
                recipientType = recipientType,
                recipientId = recipientId,
                recipientName = recipientName,
                amount = amount,
                referenceNumber = ref
            )
        )
        dao.insertAdminLog(
            AdminLogEntity(
                adminName = recipientName,
                action = "Settlement Processed",
                details = "Transferred ₹${String.format(Locale.US, "%.2f", amount)} to $recipientName (Ref: $ref)"
            )
        )
    }

    // User Authentication
    suspend fun getUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        dao.getUserByEmail(email)
    }

    suspend fun registerUser(user: UserEntity): Long = withContext(Dispatchers.IO) {
        dao.insertUser(user)
    }
}

