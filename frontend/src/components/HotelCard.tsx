import { ChevronRight, Gauge, MapPin, Star } from 'lucide-react';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import type { HotelCardData } from '../types';
import { formatMoney } from '../utils/format';

interface Props {
  hotel: HotelCardData;
  checkIn?: string;
  checkOut?: string;
}

export function HotelCard({ hotel, checkIn, checkOut }: Props) {
  const [showScore, setShowScore] = useState(false);
  const search = new URLSearchParams();
  if (checkIn) search.set('checkIn', checkIn);
  if (checkOut) search.set('checkOut', checkOut);

  const scoreRows = [
    ['Text relevance', hotel.score.textMatch, 35],
    ['Guest rating', hotel.score.rating, 25],
    ['Price value', hotel.score.priceValue, 15],
    ['Amenities', hotel.score.amenityMatch, 15],
    ['Popularity', hotel.score.popularity, 10],
  ];

  return (
    <article className="hotel-card">
      <div className="hotel-image-wrap">
        <img src={hotel.imageUrl} alt={hotel.name} className="hotel-image" loading="lazy" />
        <span className="ranking-pill"><Gauge size={14} /> Match {hotel.score.total}</span>
        <span className="rating-pill"><Star size={14} fill="currentColor" /> {hotel.rating.toFixed(1)}</span>
      </div>
      <div className="hotel-card-body">
        <div className="hotel-heading-row">
          <div>
            <h3>{hotel.name}</h3>
            <p><MapPin size={15} /> {hotel.area}, {hotel.city}</p>
          </div>
          <div className="price-copy">
            <span>from</span>
            <strong>{formatMoney(hotel.startingPrice)}</strong>
            <small>/ night</small>
          </div>
        </div>

        <div className="amenity-list">
          {hotel.amenities.slice(0, 4).map((amenity) => <span key={amenity}>{amenity}</span>)}
          {hotel.amenities.length > 4 && <span>+{hotel.amenities.length - 4}</span>}
        </div>

        <div className={`score-breakdown ${showScore ? 'expanded' : ''}`}>
          {showScore && scoreRows.map(([label, value, max]) => (
            <div className="score-row" key={String(label)}>
              <span>{label}</span>
              <div><i style={{ width: `${(Number(value) / Number(max)) * 100}%` }} /></div>
              <strong>{Number(value).toFixed(1)}</strong>
            </div>
          ))}
        </div>

        <div className="hotel-card-footer">
          <button className="text-button" onClick={() => setShowScore(!showScore)}>
            {showScore ? 'Hide ranking logic' : 'Why this result?'}
          </button>
          <Link className="card-link" to={`/hotels/${hotel.id}?${search.toString()}`}>
            View stay <ChevronRight size={17} />
          </Link>
        </div>
      </div>
    </article>
  );
}
