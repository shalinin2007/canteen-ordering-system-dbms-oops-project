-- ========================================================
-- Rajalakshmi Engineering College - Canteen Ordering System
-- Seed Data: Real REC Canteen Outlets, Categories & Items
-- ========================================================

-- 1. REC Canteen Outlets as Categories
INSERT IGNORE INTO categories (id, name, description, image_url) VALUES
(1, 'REC Café',         'Main campus dining hub — breakfast, lunch & snacks all day.', ''),
(2, 'Hut Café',         'Open-air breezy café nestled among trees for light refreshments.', ''),
(3, 'Juice Corner',     'Fresh-pressed juices, smoothies & seasonal fruit blends.', ''),
(4, 'Spice Corner',     'Campus chaat shop — pani puri, bhel puri, pav bhaji & street bites.', ''),
(5, 'WokOn',            'Asian flair — freshly made stir-fries, noodles & gravies.', ''),
(6, 'Sugarcane Corner', 'Fresh-pressed sugarcane juice & sweet refreshments.', ''),
(7, 'REC Mart',         'Packaged snacks, biscuits, chocolates, chips & cold beverages.', ''),
(8, '6 Sense Garden',   'Outdoor garden café — special meals & relaxed dining.', '');

-- 2. Real REC Canteen Food Items
INSERT IGNORE INTO food_items (id, name, description, price, category_id, item_type, is_cold, available, image_url, preparation_time) VALUES

-- REC Café (cat 1) — main dining
(1,  'Idli (2 pcs)',         'Soft steamed rice cakes served with hot sambar and coconut chutney.',               25.00, 1, 'VEG',     FALSE, TRUE, '', 5),
(2,  'Masala Dosa',          'Crispy golden crepe filled with spiced potato masala, sambar & chutney.',           40.00, 1, 'VEG',     FALSE, TRUE, '', 8),
(3,  'Poori Masala',         'Three puffed pooris with spiced potato masala.',                                     35.00, 1, 'VEG',     FALSE, TRUE, '', 8),
(4,  'Veg Meals',            'Rice, sambar, rasam, 2 curries, curd, appalam & payasam.',                          60.00, 1, 'VEG',     FALSE, TRUE, '', 6),
(5,  'Chicken Biryani',      'Aromatic basmati rice with tender spiced chicken, served with onion raita.',        80.00, 1, 'NON_VEG', FALSE, TRUE, '', 10),
(6,  'Egg Dosa',             'Crispy dosa topped with spiced scrambled egg & onion.',                             40.00, 1, 'NON_VEG', FALSE, TRUE, '', 8),
(7,  'Chapati (3 pcs)',      'Soft whole wheat flatbreads served with vegetable curry.',                           30.00, 1, 'VEG',     FALSE, TRUE, '', 7),
(8,  'Veg Biryani',          'Fragrant basmati rice cooked with fresh vegetables & whole spices.',                60.00, 1, 'VEG',     FALSE, TRUE, '', 10),

-- Hut Café (cat 2) — light refreshments
(9,  'Vada (2 pcs)',         'Crispy medu vada served with sambar and coconut chutney.',                          25.00, 2, 'VEG',     FALSE, TRUE, '', 5),
(10, 'Bread Omelette',       'Fluffy spiced egg omelette on toasted bread slices.',                               30.00, 2, 'NON_VEG', FALSE, TRUE, '', 5),
(11, 'Upma',                 'Hot rava upma tempered with mustard, curry leaves & cashews.',                      20.00, 2, 'VEG',     FALSE, TRUE, '', 5),
(12, 'Pongal',               'Creamy rice and lentil pongal seasoned with pepper, ginger & ghee.',               25.00, 2, 'VEG',     FALSE, TRUE, '', 5),

-- Juice Corner (cat 3) — fresh juices
(13, 'Fresh Orange Juice',   'Cold-pressed pure orange juice, freshly squeezed to order.',                        30.00, 3, 'BEVERAGE', TRUE,  TRUE, '', 3),
(14, 'Watermelon Juice',     'Chilled fresh watermelon blend — naturally sweet & hydrating.',                     25.00, 3, 'BEVERAGE', TRUE,  TRUE, '', 3),
(15, 'Mixed Fruit Smoothie', 'Thick seasonal fruit blend — banana, mango, apple & more.',                        40.00, 3, 'BEVERAGE', TRUE,  TRUE, '', 4),
(16, 'Mango Juice',          'Pure sweetened mango pulp drink — Mango Rs 12.',                                    12.00, 3, 'BEVERAGE', TRUE,  TRUE, '', 2),

-- Spice Corner (cat 4) — street food
(17, 'Pani Puri (6 pcs)',    'Crispy hollow puris filled with spiced potato & tangy tamarind water.',             30.00, 4, 'VEG',     FALSE, TRUE, '', 5),
(18, 'Bhel Puri',            'Puffed rice tossed with sev, onions, tomatoes & chutneys.',                        25.00, 4, 'VEG',     FALSE, TRUE, '', 4),
(19, 'Pav Bhaji',            'Spicy mashed vegetable curry served with buttered pav & lemon.',                   50.00, 4, 'VEG',     FALSE, TRUE, '', 7),
(20, 'Samosa (2 pcs)',       'Golden crispy pastry stuffed with spiced potato & peas.',                          25.00, 4, 'VEG',     FALSE, TRUE, '', 3),
(21, 'Gobi Manchurian',      'Crispy cauliflower tossed in spicy Indo-Chinese sauce.',                           50.00, 4, 'VEG',     FALSE, TRUE, '', 8),

-- WokOn (cat 5) — Chinese / Asian
(22, 'Veg Fried Rice',       'Wok-tossed rice with fresh vegetables in mild soy seasoning.',                     60.00, 5, 'VEG',     FALSE, TRUE, '', 10),
(23, 'Chicken 65',           'Crispy deep-fried spiced chicken — a classic REC favourite.',                      70.00, 5, 'NON_VEG', FALSE, TRUE, '', 10),
(24, 'Egg Hakka Noodles',    'Tossed noodles with scrambled egg, cabbage & schezwan spice.',                    70.00, 5, 'NON_VEG', FALSE, TRUE, '', 10),
(25, 'Veg Noodles',          'Stir-fried hakka noodles with mixed vegetables in soy sauce.',                     60.00, 5, 'VEG',     FALSE, TRUE, '', 10),
(26, 'Paneer Manchurian',    'Soft paneer cubes in spicy Indo-Chinese manchurian sauce.',                        65.00, 5, 'VEG',     FALSE, TRUE, '', 10),

-- Sugarcane Corner (cat 6)
(27, 'Sugarcane Juice',      'Fresh cold-pressed sugarcane juice — naturally sweet & energizing.',               20.00, 6, 'BEVERAGE', TRUE,  TRUE, '', 2),
(28, 'Sugarcane + Lemon',    'Fresh sugarcane juice with a squeeze of lemon and a hint of ginger.',              25.00, 6, 'BEVERAGE', TRUE,  TRUE, '', 2),
(29, 'Sugarcane + Ginger',   'Refreshing sugarcane juice blended with fresh ginger.',                            25.00, 6, 'BEVERAGE', TRUE,  TRUE, '', 2),

-- REC Mart (cat 7) — packaged items from vending machine
(30, 'Kurkure',              'Crunchy masala puffed corn snack — campus favourite.',                             10.00, 7, 'VEG',     FALSE, TRUE, '', 1),
(31, 'Hide & Seek Biscuits', 'Parle cream-filled chocolate sandwich biscuits.',                                  10.00, 7, 'VEG',     FALSE, TRUE, '', 1),
(32, 'Bovonto',              'Iconic Tamil Nadu grape-flavoured cold soda — chilled.',                           30.00, 7, 'BEVERAGE', TRUE,  TRUE, '', 1),
(33, 'Vibro Soda',           'Chilled carbonated soda drink.',                                                   30.00, 7, 'BEVERAGE', TRUE,  TRUE, '', 1),
(34, 'Frelish Cold Coffee',  'Ready-to-drink chilled cold coffee — Frelish brand.',                              60.00, 7, 'BEVERAGE', TRUE,  TRUE, '', 1),
(35, 'Frelish Smoothie',     'Frelish Greek Yogurt Smoothie — thick and refreshing.',                            60.00, 7, 'BEVERAGE', TRUE,  TRUE, '', 1),
(36, 'Cavins Drink',         'Cavins flavoured milk drink — chocolate / strawberry.',                            40.00, 7, 'BEVERAGE', TRUE,  TRUE, '', 1),
(37, 'Winkies Love Bite',    'Winkies chocolate mini cake snack — sweet treat.',                                 25.00, 7, 'VEG',     FALSE, TRUE, '', 1),
(38, 'Milkybar',             'Nestlé Milkybar white chocolate — smooth & creamy.',                               25.00, 7, 'VEG',     FALSE, TRUE, '', 1),
(39, 'Bisleri Water Bottle', 'Sealed 1L Bisleri mineral water bottle.',                                         10.00, 7, 'BEVERAGE', TRUE,  TRUE, '', 1),

-- 6 Sense Garden (cat 8) — outdoor special
(40, 'Garden Veg Thali',     'Special outdoor thali — rice, dal, 3 curries, roti, dessert.',                    90.00, 8, 'VEG',     FALSE, TRUE, '', 15),
(41, 'BBQ Corn',             'Charcoal-grilled sweet corn rubbed with butter, lime & chilli.',                   30.00, 8, 'VEG',     FALSE, TRUE, '', 6),
(42, 'Lemonade',             'Freshly squeezed chilled lemonade with a pinch of salt.',                          20.00, 8, 'BEVERAGE', TRUE,  TRUE, '', 2),
(43, 'Filter Coffee',        'Traditional South Indian filter coffee — strong, frothy, hot.',                    20.00, 8, 'BEVERAGE', FALSE, TRUE, '', 3),
(44, 'Masala Tea',           'Freshly brewed spiced ginger cardamom chai — hot & comforting.',                   15.00, 8, 'BEVERAGE', FALSE, TRUE, '', 3);
