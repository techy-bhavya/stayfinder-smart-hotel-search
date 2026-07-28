export type Role = 'USER' | 'ADMIN';

export interface AuthUser {
  token: string;
  userId: number;
  name: string;
  email: string;
  role: Role;
}

export interface ScoreBreakdown {
  total: number;
  textMatch: number;
  rating: number;
  priceValue: number;
  amenityMatch: number;
  popularity: number;
}

export interface HotelCardData {
  id: number;
  name: string;
  city: string;
  area: string;
  imageUrl: string;
  rating: number;
  reviewCount: number;
  startingPrice: number;
  amenities: string[];
  score: ScoreBreakdown;
}

export interface SearchResponse {
  hotels: HotelCardData[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  cached: boolean;
  elapsedMs: number;
}

export interface RoomData {
  id: number;
  roomType: string;
  pricePerNight: number;
  capacity: number;
  available: boolean;
}

export interface ReviewData {
  id: number;
  userName: string;
  rating: number;
  comment: string;
  createdAt: string;
}

export interface HotelDetails {
  id: number;
  name: string;
  city: string;
  area: string;
  description: string;
  imageUrl: string;
  rating: number;
  reviewCount: number;
  amenities: string[];
  rooms: RoomData[];
  reviews: ReviewData[];
}

export interface BookingData {
  id: number;
  hotelId: number;
  hotelName: string;
  city: string;
  imageUrl: string;
  roomId: number;
  roomType: string;
  checkIn: string;
  checkOut: string;
  guests: number;
  nights: number;
  totalAmount: number;
  status: 'CONFIRMED' | 'CANCELLED';
  createdAt: string;
}

export interface AnalyticsData {
  kpis: {
    totalRevenue: number;
    confirmedBookings: number;
    occupancyRate: number;
    cancellationRate: number;
    topCity: string;
  };
  monthlyRevenue: Array<{ month: string; revenue: number; bookings: number }>;
  cityPerformance: Array<{
    city: string;
    revenue: number;
    bookings: number;
    averageBookingValue: number;
  }>;
  topProperties: Array<{
    hotelId: number;
    hotelName: string;
    city: string;
    revenue: number;
    bookings: number;
  }>;
}
