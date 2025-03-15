package com.socialsports.util;

import com.socialsports.model.Event;
import com.socialsports.model.EventStatus;
import com.socialsports.model.SportType;
import com.socialsports.model.User;
import com.socialsports.repository.EventRepository;
import com.socialsports.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static java.util.Map.of;

/**
 * TestDataLoader is a utility class that populates the database with test data.
 * It is activated only in the "test" profile and runs once when the application starts.
 */
@Component
@Profile("test")
public class TestDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final Random random = new Random();
    
    @Value("${test.data.load:false}")
    private boolean shouldLoadTestData;

    @Autowired
    public TestDataLoader(UserRepository userRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        System.out.println("TestDataLoader instantiated successfully!");
    }

    @Override
    public void run(String... args) {
        System.out.println("TestDataLoader.run() method called!");
        System.out.println("Should load test data: " + shouldLoadTestData);
        
        if (!shouldLoadTestData) {
            System.out.println("Test data loading is disabled. Set test.data.load=true in application-test.properties to enable.");
            return;
        }
        
        System.out.println("Starting to load test data...");
        
        try {
            // Create test users
            List<User> users = createTestUsers();
            
            // Create test events
            createTestEvents(users);
            
            System.out.println("Test data loaded successfully!");
        } catch (Exception e) {
            System.err.println("Error loading test data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<User> createTestUsers() {
        System.out.println("Creating test users...");
        List<User> users = new ArrayList<>();
        
        // Dutch and international names for expat community
        List<String> userNames = Arrays.asList(
            "Jeroen de Vries", "Sophie Müller", "Mohammed El-Fassi", "Emma Jansen", 
            "Thomas Berg", "Fatima Yilmaz", "Liam O'Connor", "Anouk van der Meer", 
            "Carlos Rodriguez", "Mei Lin", "Sven Andersson", "Priya Sharma", 
            "Jan-Willem Bakker", "Anna Kowalski", "David Goldstein"
        );
        
        List<String> emails = Arrays.asList(
            "jeroen.devries@gmail.com", "sophie.m@expat.nl", "m.elfassi@hotmail.com", 
            "emma.jansen@gmail.com", "thomas.berg@outlook.com", "fatima.y@gmail.com", 
            "liam.oconnor@gmail.com", "anouk.meer@yahoo.com", "carlos.r@gmail.com", 
            "mei.lin@outlook.com", "sven.andersson@gmail.com", "priya.s@hotmail.com", 
            "janwillem@gmail.com", "anna.k@gmail.com", "david.g@gmail.com"
        );
        
        List<String> phoneNumbers = Arrays.asList(
            "+31612345678", "+31623456789", "+31634567890", "+31645678901", 
            "+31656789012", "+31667890123", "+31678901234", "+31689012345", 
            "+31690123456", "+31601234567", "+31701234567", "+31712345678", 
            "+31723456789", "+31734567890", "+31745678901"
        );
        
        // Create users with different attributes
        for (int i = 0; i < userNames.size(); i++) {
            LocalDateTime createdAt = LocalDateTime.now().minus(random.nextInt(180) + 30, ChronoUnit.DAYS);
            
            User user = User.builder()
                .userId(UUID.randomUUID().toString())
                .name(userNames.get(i))
                .email(emails.get(i))
                .phoneNumber(phoneNumbers.get(i))
                .skillLevel(random.nextInt(5) + 1) // Skill levels from 1-5
                .eventsCreated(random.nextInt(10))
                .eventsJoined(random.nextInt(20))
                .isPremium(random.nextBoolean())
                .whatsappLinked(true)
                .createdAt(createdAt)
                .updatedAt(createdAt.plus(random.nextInt(30), ChronoUnit.DAYS))
                .build();
            
            users.add(user);
            userRepository.save(user);
            System.out.println("Created test user: " + user.getName());
        }
        
        System.out.println("Created " + users.size() + " test users successfully");
        return users;
    }

    private void createTestEvents(List<User> users) {
        System.out.println("Creating test events...");
        
        // Define location data for Dutch sports venues
        Map<String, String> locations = new HashMap<>();
        locations.put("Amsterdam", "Padel City Amstelveen, Amsterdamseweg 130, 1182 HK Amstelveen");
        locations.put("Amsterdam", "Sportpark Sloterdijk, Wethouder van Essenweg 12, 1043 WB Amsterdam");
        locations.put("Amsterdam", "Frans Otten Stadion, IJsbaanpad 43, 1076 CV Amsterdam");
        locations.put("Rotterdam", "Victoria Padel Rotterdam, Abraham van Stolkweg 31, 3041 JA Rotterdam");
        locations.put("Rotterdam", "Kralingen Sportclub, Stieltjesweg 3, 3033 LA Rotterdam");
        locations.put("Utrecht", "Sportcentrum Olympos, Uppsalalaan 3, 3584 CT Utrecht");
        locations.put("Utrecht", "Padel Utrecht Oost, Grebbeberglaan 7, 3527 VX Utrecht");
        locations.put("Den Haag", "The Padel Club Binckhorst, Saturnusstraat 60, 2516 AH Den Haag");
        locations.put("Den Haag", "Sportcentrum de Uithof, Jaap Edenweg 10, 2544 NL Den Haag");
        locations.put("Eindhoven", "David Lloyd Eindhoven, Amazonelaan 4, 5623 LK Eindhoven");
        locations.put("Eindhoven", "Urban Sports Strijp-S, Beukenlaan 1, 5616 LA Eindhoven");
        
        // Create a distribution of events (past, current, future)
        int totalEvents = 30; // Create 30 test events
        int pastEvents = totalEvents / 4;           // 25%
        int currentEvents = totalEvents / 10;       // 10%
        int nearFutureEvents = (totalEvents * 4) / 10; // 40%
        int farFutureEvents = totalEvents - pastEvents - currentEvents - nearFutureEvents; // 25%
        
        System.out.println("Creating " + pastEvents + " past events (COMPLETED)");
        // Create past events (completed)
        createEventsWithTimeframe(users, pastEvents, -90, -1, EventStatus.COMPLETED, locations);
        
        System.out.println("Creating " + currentEvents + " current events (CONFIRMED)");
        // Create current events (today/tomorrow)
        createEventsWithTimeframe(users, currentEvents, 0, 1, EventStatus.CONFIRMED, locations);
        
        System.out.println("Creating " + nearFutureEvents + " near future events (CONFIRMED)");
        // Create near future events (next 2 weeks)
        createEventsWithTimeframe(users, nearFutureEvents, 2, 14, EventStatus.CONFIRMED, locations);
        
        System.out.println("Creating " + farFutureEvents + " far future events (CONFIRMED)");
        // Create far future events (next 1-2 months)
        createEventsWithTimeframe(users, farFutureEvents, 15, 60, EventStatus.CONFIRMED, locations);
        
        System.out.println("Creating 4 cancelled events");
        // Create some cancelled events
        createCancelledEvents(users, 4, locations);
        
        System.out.println("Created all test events successfully");
    }
    
    private void createEventsWithTimeframe(List<User> users, int count, int minDays, int maxDays, 
                                          EventStatus status, Map<String, String> locations) {
        List<String> cities = new ArrayList<>(locations.keySet());
        List<SportType> sportTypes = Arrays.asList(SportType.values());
        
        IntStream.range(0, count).forEach(i -> {
            User creator = users.get(random.nextInt(users.size()));
            SportType sportType = sportTypes.get(random.nextInt(sportTypes.size()));
            
            // Determine participant limit based on sport type and add some variation
            int baseParticipantLimit = sportType.getMaxPlayers();
            int participantLimit = Math.min(baseParticipantLimit, sportType.getMinPlayers() + random.nextInt(baseParticipantLimit - sportType.getMinPlayers() + 1));
            
            // Set event time
            int dayOffset = random.nextInt(maxDays - minDays + 1) + minDays;
            LocalDateTime eventTime = LocalDateTime.now().plusDays(dayOffset)
                                    .withHour(9 + random.nextInt(12)) // Between 9 AM and 9 PM
                                    .withMinute(random.nextInt(4) * 15) // 0, 15, 30, or 45 minutes
                                    .truncatedTo(ChronoUnit.MINUTES);
            
            // Select random location
            String city = cities.get(random.nextInt(cities.size()));
            List<String> cityLocations = locations.entrySet().stream()
                                        .filter(e -> e.getKey().equals(city))
                                        .map(Map.Entry::getValue)
                                        .collect(Collectors.toList());
            String location = cityLocations.get(random.nextInt(cityLocations.size()));
            
            // Determine how full the event should be
            int fullnessCategory = random.nextInt(4); // 0-3
            int currentParticipants;
            
            if (fullnessCategory == 0) { // Full (20%)
                currentParticipants = participantLimit;
            } else if (fullnessCategory == 1) { // Nearly full (30%)
                currentParticipants = Math.max(1, participantLimit - (1 + random.nextInt(2)));
            } else if (fullnessCategory == 2) { // Half full (30%)
                currentParticipants = Math.max(1, participantLimit / 2 + random.nextInt(3) - 1);
            } else { // New/Empty (20%)
                currentParticipants = 1 + random.nextInt(2);
            }
            
            currentParticipants = Math.min(currentParticipants, users.size());
            
            // Select participants randomly
            List<User> allUsers = new ArrayList<>(users);
            Collections.shuffle(allUsers);
            List<User> participants = allUsers.subList(0, currentParticipants);
            
            if (!participants.contains(creator)) {
                participants.set(0, creator); // Ensure creator is a participant
            }
            
            // Collect participant phone numbers
            List<String> participantPhoneNumbers = participants.stream()
                                                 .map(User::getPhoneNumber)
                                                 .collect(Collectors.toList());
            
            // Skill level for event
            int skillLevel = random.nextInt(5) + 1; // 1-5
            
            // Create whatsapp group and booking links
            String whatsappGroupId = null;
            String bookingLink = null;
            
            int linkType = random.nextInt(4);
            if (linkType == 0 || linkType == 1) {
                whatsappGroupId = "https://chat.whatsapp.com/example" + (i + 1);
            }
            if (linkType == 0 || linkType == 2) {
                bookingLink = "https://" + sportType.name().toLowerCase() + "booking.nl/booking/" + 
                             city.toLowerCase() + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMdd"));
            }
            
            // Create creation time
            LocalDateTime createdAt = eventTime.minusDays(random.nextInt(30) + 1);
            if (createdAt.isAfter(LocalDateTime.now())) {
                createdAt = LocalDateTime.now().minusDays(random.nextInt(7) + 1);
            }
            
            // Create a new event
            Event event = Event.builder()
                    .id(UUID.randomUUID().toString())
                    .sportType(sportType)
                    .location(location)
                    .eventTime(eventTime)
                    .creatorPhoneNumber(creator.getPhoneNumber())
                    .participantPhoneNumbers(participantPhoneNumbers)
                    .participantLimit(participantLimit)
                    .skillLevel(skillLevel)
                    .status(status)
                    .whatsappGroupId(whatsappGroupId)
                    .bookingLink(bookingLink)
                    .createdAt(createdAt)
                    .updatedAt(createdAt.plus(random.nextInt(5), ChronoUnit.DAYS))
                    .remindersSent(new HashMap<>(Map.of("24h", false, "2h", false)))
                    .build();
            
            eventRepository.save(event);
            System.out.println("Created test event: " + sportType + " at " + location + " on " + eventTime);
        });
    }
    
    private void createCancelledEvents(List<User> users, int count, Map<String, String> locations) {
        // Create some cancelled events (mix of past and future dates)
        List<String> cities = new ArrayList<>(locations.keySet());
        List<SportType> sportTypes = Arrays.asList(SportType.values());
        
        IntStream.range(0, count).forEach(i -> {
            User creator = users.get(random.nextInt(users.size()));
            SportType sportType = sportTypes.get(random.nextInt(sportTypes.size()));
            
            // Set event time (mix of past and future)
            int dayOffset = random.nextInt(60) - 30; // -30 to +30 days
            LocalDateTime eventTime = LocalDateTime.now().plusDays(dayOffset)
                                    .withHour(9 + random.nextInt(12))
                                    .withMinute(random.nextInt(4) * 15)
                                    .truncatedTo(ChronoUnit.MINUTES);
            
            // Select random location
            String city = cities.get(random.nextInt(cities.size()));
            List<String> cityLocations = locations.entrySet().stream()
                                        .filter(e -> e.getKey().equals(city))
                                        .map(Map.Entry::getValue)
                                        .collect(Collectors.toList());
            String location = cityLocations.get(random.nextInt(cityLocations.size()));
            
            // Create participants (several that joined and then cancelled)
            int participantLimit = sportType.getMinPlayers() + random.nextInt(sportType.getMaxPlayers() - sportType.getMinPlayers() + 1);
            int initialParticipants = Math.min(participantLimit, 3 + random.nextInt(4));
            initialParticipants = Math.min(initialParticipants, users.size());
            
            // Select participants randomly
            List<User> allUsers = new ArrayList<>(users);
            Collections.shuffle(allUsers);
            List<User> participants = allUsers.subList(0, initialParticipants);
            
            if (!participants.contains(creator)) {
                participants.set(0, creator); // Ensure creator is a participant
            }
            
            // For cancelled events, simulate cancellations by having fewer participants
            int finalParticipants = Math.max(1, initialParticipants / 2);
            List<String> participantPhoneNumbers = participants.subList(0, finalParticipants).stream()
                                                 .map(User::getPhoneNumber)
                                                 .collect(Collectors.toList());
            
            // Create event
            Event event = Event.builder()
                .id(UUID.randomUUID().toString())
                .sportType(sportType)
                .location(location)
                .eventTime(eventTime)
                .creatorPhoneNumber(creator.getPhoneNumber())
                .participantPhoneNumbers(Collections.singletonList(creator.getPhoneNumber()))
                .participantLimit(sportType.getMaxPlayers())
                .skillLevel(random.nextInt(5) + 1)
                .status(EventStatus.CANCELED)
                .createdAt(LocalDateTime.now().minusDays(random.nextInt(90)))
                .updatedAt(LocalDateTime.now().minusDays(random.nextInt(30)))
                .remindersSent(new HashMap<>(of("24h", false, "2h", false)))
                .build();
            
            eventRepository.save(event);
            System.out.println("Created cancelled test event: " + sportType + " at " + location);
        });
    }
} 