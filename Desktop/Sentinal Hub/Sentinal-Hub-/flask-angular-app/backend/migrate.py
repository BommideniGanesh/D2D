from app import create_app, db
from app.models import User, Item, KycDetails

if __name__ == '__main__':
    app = create_app()
    with app.app_context():
        try:
            # Need to disable foreign key checks temporarily for dropping tables in MySQL
            db.session.execute(db.text('SET FOREIGN_KEY_CHECKS = 0;'))
            db.drop_all()
            db.session.execute(db.text('SET FOREIGN_KEY_CHECKS = 1;'))
            db.create_all()
            
            # Seed mock items for dashboard
            if not Item.query.first():
                items = [
                    Item(name="Market Alpha", description="Primary trading algorithm"),
                    Item(name="Sentiment Beta", description="News scraping module"),
                    Item(name="Risk Guard", description="Automated stop-loss systems")
                ]
                db.session.add_all(items)
            db.session.commit()
            print("Database successfully reset and seeded!")
        except Exception as e:
            print(f"Error resetting database: {e}")
